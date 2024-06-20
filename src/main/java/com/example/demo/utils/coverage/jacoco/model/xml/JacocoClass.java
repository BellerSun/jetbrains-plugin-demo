package com.example.demo.utils.coverage.jacoco.model.xml;

import com.example.demo.utils.coverage.jacoco.JacocoAnalyzer;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Method;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class JacocoClass implements JacocoAnalyzer.JacocoCountableClass {
    @XmlAttribute
    private String name;

    @XmlAttribute(name = "sourcefilename")
    private String sourceFileName;

    @XmlElement(name = "method")
    private List<JacocoMethod> methods;

    @XmlElement(name = "counter")
    private List<JacocoCounter> counters;

    /**
     * 获取指定方法名的覆盖率信息
     */
    public JacocoMethod getJacocoMethod(String methodName) {
        return methods.stream()
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取指定方法的覆盖率信息
     */
    public JacocoMethod getJacocoMethod(Method method) {
        return methods.stream()
                .filter(m -> m.getName().equals(method.getName()))
                //.filter(m -> m.getDesc().equals(method.getDescriptor()))
                .findFirst()
                .orElse(null);
    }
}
