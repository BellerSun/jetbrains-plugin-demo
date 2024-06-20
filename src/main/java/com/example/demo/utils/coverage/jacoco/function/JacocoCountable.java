package com.example.demo.utils.coverage.jacoco.function;

import com.example.demo.utils.coverage.jacoco.model.constant.JacocoCounterType;
import com.example.demo.utils.coverage.jacoco.model.xml.JacocoCounter;

import java.util.List;

@SuppressWarnings("unused")
public interface JacocoCountable {
    List<JacocoCounter> getCounters();

    default JacocoCounter getCounter(String type) {
        return getCounters().stream()
                .filter(c -> c.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(new JacocoCounter(type));
    }

    default JacocoCounter getInstructionCounter() {
        return getCounter(JacocoCounterType.INSTRUCTION);
    }

    default JacocoCounter getComplexityCounter() {
        return getCounter(JacocoCounterType.COMPLEXITY);
    }

    default JacocoCounter getBranchCounter() {
        return getCounter(JacocoCounterType.BRANCH);
    }

    default JacocoCounter getLineCounter() {
        return getCounter(JacocoCounterType.LINE);
    }

    default JacocoCounter getMethodCounter() {
        return getCounter(JacocoCounterType.METHOD);
    }

}
