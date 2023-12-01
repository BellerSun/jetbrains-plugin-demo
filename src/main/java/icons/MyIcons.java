package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
@SuppressWarnings("unused")
public interface MyIcons {

    Icon logoPng = IconLoader.getIcon("/icons/16x16/logo.png", MyIcons.class);

    Icon logo = IconLoader.getIcon("/icons/16x16/logo.svg", MyIcons.class);

    Icon stocking = IconLoader.getIcon("/icons/16x16/stocking.png", MyIcons.class);
}
