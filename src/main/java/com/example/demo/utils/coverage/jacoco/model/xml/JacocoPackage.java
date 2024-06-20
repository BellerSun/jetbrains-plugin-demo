package com.example.demo.utils.coverage.jacoco.model.xml;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class JacocoPackage {
    @XmlAttribute
    private String name;

    @XmlElement(name = "class")
    private List<JacocoClass> classes;

    /**
     * 获取指定类名的覆盖率信息
     */
    public JacocoClass getJacocoClass(String packageName,String className) {
        final String classFullNameRegular = packageName.replace(".", "/") + "/" + className;
        return classes.stream()
                .filter(c -> c.getName().equals(classFullNameRegular))
                .findFirst()
                .orElse(null);
    }
}
