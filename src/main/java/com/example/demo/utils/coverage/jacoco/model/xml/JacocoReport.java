package com.example.demo.utils.coverage.jacoco.model.xml;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
public class JacocoReport {
    @XmlAttribute
    private String name;

    @XmlElement(name = "package")
    private List<JacocoPackage> packages;

    /**
     * 获取指定包名的覆盖率信息
     *
     * @param packageName 包名
     */
    public JacocoPackage getJacocoPackage(String packageName) {
        final String packageRegular = packageName.replace(".", "/");
        return packages.stream()
                .filter(p -> p.getName().equals(packageRegular))
                .findFirst()
                .orElse(null);
    }
}
