package ai.codegeex.plugin.actions;

import ai.codegeex.plugin.CodegeexIcons;
import ai.codegeex.plugin.settings.CodegeexApplicationSettings;
import ai.codegeex.plugin.settings.CodegeexApplicationState;
import ai.codegeex.plugin.lang.agent.dto.CommitMessageParam;
import ai.codegeex.plugin.lang.agent.dto.CommitMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.ui.CommitMessage;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.NetworkInterface;
import java.net.ProxySelector;
import java.net.SocketException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class CommitMessageSuggestionAction extends AnAction implements DumbAware {
    private Inlay inlay = null;

    @Override
    public void update(@NotNull AnActionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("AnActionEvent must not be null");
        }

        if (CodegeexApplicationSettings.settings().useOpenAIAPI) {
            event.getPresentation().setEnabledAndVisible(false);
            return;
        }

        CommitMessage commitMessage = event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
        if (commitMessage == null || !commitMessage.getEditorField().isEnabled()) {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setIcon(CodegeexIcons.StatusBarCompletionInProgress);
            return;
        }

        VcsPrompting vcsPrompting = ServiceManager.getService(event.getProject(), VcsPrompting.class);
        List<Object> prompts = vcsPrompting != null ? vcsPrompting.getPrompts() : Collections.emptyList();
        event.getPresentation().setIcon(CodegeexIcons.CODEGEEX);
        event.getPresentation().setEnabled(!prompts.isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("AnActionEvent must not be null");
        }

        Project project = event.getProject();
        if (project == null) {
            return;
        }

        String sessionId = CodegeexApplicationSettings.settings().sessionId;
        if (sessionId.isEmpty()) {
            promptForLogin(project);
            return;
        }

        ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        String ideVersion = getIdeVersion(applicationInfo);

        List<Change> changes = VcsUtil.getChangesUsingUI(event);
        if (changes == null) {
            return;
        }

        VcsPrompting vcsPrompting = ServiceManager.getService(project, VcsPrompting.class);
        String diffSummary = vcsPrompting.getDiffSummary(changes);
        if (diffSummary.isEmpty() || diffSummary.equals("\n")) {
            notifyUserOfEmptyDiff(project);
            return;
        }

        CommitMessage commitMessage = event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
        commitMessage.getEditorField().setText("Generating...");
        commitMessage.getEditorField().setEnabled(false);

        ApplicationManager.getApplication().executeOnPooledThread(() -> generateCommitMessage(event, project, sessionId, diffSummary));
    }

    private void generateCommitMessage(AnActionEvent event, Project project, String sessionId, String diffSummary) {
        CodegeexApplicationState appState = CodegeexApplicationSettings.settings();
        event.getPresentation().setIcon(CodegeexIcons.StatusBarCompletionInProgress);

        try {
            SSLContext sslContext = createSslContext();
            if (sslContext == null) {
                return;
            }

            String macAddress = getMacAddress();
            appState.macAddress = macAddress;

            CommitMessageRequest commitMessageRequest = createCommitMessageRequest(diffSummary, macAddress);
            String requestBody = new ObjectMapper().writeValueAsString(commitMessageRequest);

            sendHttpRequest(event, project, sessionId, requestBody, sslContext);

            event.getPresentation().setIcon(CodegeexIcons.CODEGEEX);
        } catch (Exception e) {
            handleException(event, e);
        }
    }

    private SSLContext createSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{new SimpleTrustManager()}, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            return null;
        }
    }

    private String getMacAddress() throws SocketException {
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            byte[] hardwareAddress = networkInterface.getHardwareAddress();
            if (hardwareAddress == null || networkInterface.isVirtual() || !networkInterface.isUp() || networkInterface.isLoopback()) {
                continue;
            }

            StringBuilder macBuilder = new StringBuilder();
            for (int i = 0; i < hardwareAddress.length; i++) {
                macBuilder.append(String.format("%02X%s", hardwareAddress[i], (i < hardwareAddress.length - 1) ? "-" : ""));
            }
            return macBuilder.toString();
        }
        return "";
    }

    private CommitMessageRequest createCommitMessageRequest(String diffSummary, String macAddress) {
        CommitMessageRequest request = new CommitMessageRequest();
        request.setMachineId(macAddress);
        request.setLocale(determineLocale());
        request.setModel("codegeex-pro");
        request.setCommand("commit_message_v1");

        CommitMessageParam param = new CommitMessageParam();
        param.setGit_diff(diffSummary);
        param.setCommit_history(diffSummary);
        param.setCommit_type(determineCommitType());

        request.setCommit_message(param);
        return request;
    }

    private String determineLocale() {
        String chatLanguage = CodegeexApplicationSettings.settings().chatLanguageSettingEnum;
        if ("zh-CN".equals(chatLanguage) || ("Default".equals(chatLanguage) && isSystemLocaleChinese())) {
            return "zh";
        }
        return "en";
    }

    private boolean isSystemLocaleChinese() {
        // Replace with actual implementation to determine system locale
        return false;
    }

    private String determineCommitType() {
        String commitType = CodegeexApplicationSettings.settings().commitMessageEnum;
        switch (commitType) {
            case "ConventionalCommits":
                return "conventional";
            case "ReferToCommitLog":
                return "auto";
            default:
                return "default";
        }
    }

    private void sendHttpRequest(AnActionEvent event, Project project, String sessionId, String requestBody, SSLContext sslContext) {
        ProxySelector proxySelector = ProxySelector.getDefault();
        HttpRequest.post(CodegeexApplicationSettings.settings().getApiUrl("/code/chatCodeSseV3/chat?stream=false"))
                .readTimeout(Duration.ofMinutes(2))
                .bodyString(requestBody)
                .addHeader("code-token", sessionId)
                .sslSocketFactory(sslContext.getSocketFactory(), new SimpleTrustManager())
                .hostnameVerifier((hostname, session) -> true)
                .proxySelector(proxySelector)
                .async()
                .onFailed((request, httpException) -> handleHttpRequestFailure(event, project, httpException))
                .onSuccessful(response -> handleHttpRequestSuccess(event, response));
    }

    private void handleHttpRequestFailure(AnActionEvent event, Project project, HttpRequest.HttpException httpException) {
        if (httpException.getResponse().code() == 401) {
            promptForLogin(project);
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            CommitMessage commitMessage = event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
            commitMessage.getEditorField().setText("");
            commitMessage.getEditorField().setEnabled(true);
        });

        httpException.printStackTrace();
    }

    private void handleHttpRequestSuccess(AnActionEvent event, HttpRequest.HttpResponse response) {
        String responseBody = response.asString();
        JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
        String commitMessageText = jsonObject.get("text").getAsString();

        ApplicationManager.getApplication().invokeLater(() -> {
            CommitMessage commitMessage = event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
            commitMessage.getEditorField().setEnabled(true);
            commitMessage.getEditorField().setText(commitMessageText);
        });
    }

    private void handleException(AnActionEvent event, Exception e) {
        CommitMessage commitMessage = event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL);
        if (commitMessage != null) {
            commitMessage.getEditorField().setEnabled(true);
        }
        event.getPresentation().setIcon(CodegeexIcons.StatusBarIconError);
        e.printStackTrace();
    }

    private void promptForLogin(Project project) {
        // Implement logic to prompt user for login
    }

    private void notifyUserOfEmptyDiff(Project project) {
        // Implement logic to notify user of empty diff
    }

    private String getIdeVersion(ApplicationInfo applicationInfo) {
        try {
            return applicationInfo.getMajorVersion();
        } catch (Exception e) {
            System.out.println("Error getting IDE version");
            return "";
        }
    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private static class SimpleTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {}

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {}

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }
}
