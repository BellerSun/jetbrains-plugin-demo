package com.example.demo.utils.coverage.jacoco.model.constant;

/**
 * jacoco的counter的类型
 */
public final class JacocoCounterType {
    /**
     * 指令覆盖率<br/>
     * <p>
     * <ol>
     *     <li>定义：指令覆盖率衡量的是被测试代码中的每个字节码指令是否被执行过。</li>
     *     <li>含义：指令覆盖率的高低表示代码中的每个具体操作（如赋值、计算等）是否被测试覆盖。它是最细粒度的覆盖率指标。</li>
     * </ol>
     */
    public static final String INSTRUCTION = "INSTRUCTION";
    /**
     * 复杂度覆盖率<br/>
     * <p>
     * <ol>
     *     <li>定义：复杂度覆盖率衡量的是代码中的控制流复杂度，也称为圈复杂度。</li>
     *     <li>含义：复杂度覆盖率反映了代码的逻辑分支和循环结构的复杂程度。它通过计算代码中的独立路径数来衡量，即测试需要多少条独立的路径才能完全覆盖代码。</li>
     * </ol>
     */
    public static final String COMPLEXITY = "COMPLEXITY";
    /**
     * 分支覆盖率<br/>
     * <p>
     *  <ol>
     *      <li>定义：分支覆盖率衡量的是代码中的每个分支（如 if-else、switch 语句的每个条件）是否被执行过。</li>
     *      <li>含义：分支覆盖率表示代码中的每个逻辑分支是否被测试过。一个分支指的是条件判断中的每个可能的路径。</li>
     *      <li>示例：if-else 语句中的每个条件、switch 语句中的每个 case。</li>
     *      <li>计算方式：分支覆盖率 = 被覆盖的分支数 / 总分支数</li>
     * </ol>
     */
    public static final String BRANCH = "BRANCH";
    /**
     * 行覆盖率<br/>
     * <p>
     * <ol>
     *     <li>定义：行覆盖率衡量的是代码中的每一行是否被执行过。</li>
     *     <li>含义：行覆盖率表示测试是否触及到代码中的每一行。它是最常用的覆盖率指标之一，因为它直接反映了源代码的覆盖情况。</li>
     * </ol>
     */
    public static final String LINE = "LINE";
    /**
     * 方法覆盖率<br/>
     * <p>
     * <ol>
     *     <li>定义：方法覆盖率衡量的是代码中的每个方法是否被调用过。</li>
     *     <li>含义：方法覆盖率表示测试是否调用了代码中的每个方法。它提供了一种粗粒度的覆盖率指标，用于判断测试是否执行了所有的方法。</li>
     * </ol>
     */
    public static final String METHOD = "METHOD";
    /**
     * 类覆盖率<br/>
     * <p>
     * <ol>
     *     <li>定义：类覆盖率衡量的是代码中的每个类是否被使用过（即类中的任何代码是否被执行过）。</li>
     *     <li>含义：类覆盖率表示测试是否使用过代码中的每个类。它提供了一种更粗粒度的覆盖率指标，用于判断测试是否涉及到所有的类。</li>
     * </ol>
     */
    public static final String CLASS = "CLASS";
}
