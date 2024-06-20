package com.example.demo.utils.coverage.jacoco.model.xml;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class JacocoCounter {
    @XmlAttribute
    private String type;

    @XmlAttribute
    private int missed;

    @XmlAttribute
    private int covered;

    public JacocoCounter(String type) {
        this.type = type;
        this.missed = 0;
        this.covered = 0;
    }

    /**
     * 获取覆盖率
     */
    public float getCoverageRate() {
        return (float) covered / this.getTotal();
    }

    /**
     * 获取未覆盖率
     */
    public float getMissRate() {
        return (float) missed / this.getTotal();
    }

    /**
     * 获取总数
     */
    public int getTotal() {
        return missed + covered;
    }
}
