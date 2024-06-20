package com.example.demo.utils.coverage.jacoco.model.xml;

import com.example.demo.utils.coverage.jacoco.function.JacocoCountable;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class JacocoMethod implements JacocoCountable {
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String desc;

    @XmlAttribute
    private int line;

    @XmlElement(name = "counter")
    private List<JacocoCounter> counters;
}
