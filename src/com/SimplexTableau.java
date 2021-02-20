package com;

public class SimplexTableau {
    private int colLen;
    private int rowLen;
    private ConstraintType[] constraintTypes;

    private int[] basicVariables;
    private Var[] objectiveFunction;
    private Var[][] constraintMatrix;
    private Var[] z;
    private Var[] cz;

    private int iterationCount = 0;

    public SimplexTableau(Object[] of, Object[][] cm, SimplexSolveType simplexSolveType) {
        // 초기값 설정
        {
            colLen = of.length;
            rowLen = cm.length;
            constraintTypes = new ConstraintType[rowLen];

            for (int i = 0; i < rowLen; i++) {
                if ("<=".equals(cm[i][cm[i].length - 2])) {
                    constraintTypes[i] = ConstraintType.LESS_THAN;
                    colLen++;
                } else if ("=".equals(cm[i][cm[i].length - 2])) {
                    constraintTypes[i] = ConstraintType.EQUAL;
                    colLen++;
                } else if (">=".equals(cm[i][cm[i].length - 2])) {
                    constraintTypes[i] = ConstraintType.GREATER_THAN;
                    colLen += 2;
                } else {
                    throw new RuntimeException("제약 조건은 <=, =, >= 중에 하나로 지정하여야 합니다.");
                }
            }

            basicVariables = new int[rowLen];
            objectiveFunction = new Var[colLen + 1];
            constraintMatrix = new Var[rowLen][colLen + 1];
            z = new Var[colLen + 1];
            cz = new Var[colLen];
        }
        // 목적함수 표준화
        {
            int offset = 0;
            for (int i = 0; i < of.length; i++) {
                if (of[i] instanceof Var) {
                    if (simplexSolveType == SimplexSolveType.MINIMIZE) {
                        objectiveFunction[offset++] = ((Var) of[i]).mul(new Var(-1));
                    } else {
                        objectiveFunction[offset++] = (Var) of[i];
                    }
                } else {
                    if (simplexSolveType == SimplexSolveType.MINIMIZE) {
                        objectiveFunction[offset++] = new Var(((Number) of[i]).doubleValue() * -1);
                    } else {
                        objectiveFunction[offset++] = new Var(((Number) of[i]).doubleValue());
                    }
                }
            }

            for (int i = 0; i < rowLen; i++) {
                if (constraintTypes[i] == ConstraintType.LESS_THAN) {
                    objectiveFunction[offset++] = new Var(0);
                } else if (constraintTypes[i] == ConstraintType.EQUAL) {
                    objectiveFunction[offset++] = new Var(0, -1);
                } else if (constraintTypes[i] == ConstraintType.GREATER_THAN) {
                    objectiveFunction[offset++] = new Var(0);
                    objectiveFunction[offset++] = new Var(0, -1);
                }
            }
        }
        // 제약조건 표준화
        {
            for (int i = 0; i < rowLen; i++) {
                for (int j = 0; j < cm[i].length - 2; j++) {
                    if (cm[i][j] instanceof Var) {
                        constraintMatrix[i][j] = (Var) cm[i][j];
                    } else {
                        constraintMatrix[i][j] = new Var(((Number) cm[i][j]).doubleValue());
                    }
                }
            }

            int offset = of.length;

            for (int i = 0; i < rowLen; i++) {
                for (int j = of.length; j < colLen; j++) {
                    constraintMatrix[i][j] = new Var(0);
                }

                if (constraintTypes[i] == ConstraintType.LESS_THAN) {
                    basicVariables[i] = offset;
                    constraintMatrix[i][offset++] = new Var(1);
                } else if (constraintTypes[i] == ConstraintType.EQUAL) {
                    basicVariables[i] = offset;
                    constraintMatrix[i][offset++] = new Var(1);
                } else if (constraintTypes[i] == ConstraintType.GREATER_THAN) {
                    constraintMatrix[i][offset++] = new Var(-1);
                    basicVariables[i] = offset;
                    constraintMatrix[i][offset++] = new Var(1);
                }
            }

            for (int i = 0; i < rowLen; i++) {
                constraintMatrix[i][colLen] = new Var(((Number) cm[i][cm[i].length - 1]).doubleValue());
            }
        }
    }

    private void visualize() {
        System.out.printf("%s -> %d", "반복횟수", iterationCount);
        System.out.printf("%s", System.lineSeparator());

        System.out.printf("%10s|", "INDEX");
        for (int i = 0; i < colLen; i++) {
            System.out.printf("%10d|", i);
        }
        System.out.printf("%s", System.lineSeparator());

        System.out.printf("%10s|", "OBJECTIVE");
        for (int i = 0; i < colLen; i++) {
            System.out.printf("%10s|", objectiveFunction[i]);
        }
        System.out.printf("%s", System.lineSeparator());

        for (int i = 0; i < rowLen; i++) {
            System.out.printf("%10d|", basicVariables[i]);
            for (int j = 0; j < colLen; j++) {
                System.out.printf("%10s|", constraintMatrix[i][j]);
            }
            System.out.printf("%10s|", constraintMatrix[i][colLen]);
            System.out.printf("%s", System.lineSeparator());
        }

        System.out.printf("%10s|", "Z");
        for (int i = 0; i < colLen + 1; i++) {
            System.out.printf("%10s|", z[i]);
        }
        System.out.printf("%s", System.lineSeparator());

        System.out.printf("%10s|", "C-Z");
        for (int i = 0; i < colLen; i++) {
            System.out.printf("%10s|", cz[i]);
        }
        System.out.printf("%s", System.lineSeparator());
        System.out.printf("%s", System.lineSeparator());
    }

    public void solve() {
        while (true) {
            // z값 구하기
            {
                for (int i = 0; i < colLen; i++) {
                    z[i] = new Var(0);
                    for (int j = 0; j < rowLen; j++) {
                        z[i] = z[i].add(objectiveFunction[basicVariables[j]].mul(constraintMatrix[j][i]));
                    }
                }

                z[colLen] = new Var(0);
                for (int i = 0; i < rowLen; i++) {
                    z[colLen] = z[colLen].add(objectiveFunction[basicVariables[i]].mul(constraintMatrix[i][colLen]));
                }
            }

            // c-z값 구하기
            {
                for (int i = 0; i < colLen; i++) {
                    cz[i] = objectiveFunction[i].add(z[i].mul(new Var(-1)));
                }
            }

            visualize();

            // 최적해인지 판단
            {
                int enteringColIndex = -1;
                for (int i = 0; i < colLen; i++) {
                    if (cz[i].getValue() > 0) {
                        enteringColIndex = i;
                        break;
                    }
                }

                // z에 양수가 존재하면 재계산
                if (enteringColIndex == -1) {
                    if (z[colLen].getDegree() > 1) {
                        System.out.println("실행불가능 해가 발생하였습니다.");
                    } else {
                        System.out.println("최적해 => " + z[colLen]);
                    }
                    break;
                } else {
                    double min = 0;
                    int leavingRowIndex = -1;
                    double[] ratio = new double[rowLen];
                    for (int i = 0; i < rowLen; i++) {
                        if (constraintMatrix[i][enteringColIndex].getValue() <= 0) {
                            ratio[i] = -1;
                            continue;
                        }

                        ratio[i] = constraintMatrix[i][colLen].getValue() / constraintMatrix[i][enteringColIndex].getValue();
                        if (leavingRowIndex == -1) {
                            min = ratio[i];
                            leavingRowIndex = i;
                        } else {
                            if (ratio[i] < min) {
                                min = ratio[i];
                                leavingRowIndex = i;
                            }
                        }
                    }

                    for (int i = 0; i < rowLen; i++) {
                        for (int j = 0; j < rowLen; j++) {
                            if (i != j) {
                                if (ratio[i] == ratio[j] && ratio[i] > 0) {
                                    System.out.println("퇴화해가 발생하였습니다.");
                                }
                            }
                        }
                    }

                    // 탈락변수가 없으면 무한해
                    if (leavingRowIndex == -1) {
                        System.out.println("무한해가 발생하였습니다.");
                        break;
                    } else {
                        double pivot = constraintMatrix[leavingRowIndex][enteringColIndex].getValue();
                        for (int i = 0; i < colLen + 1; i++) {
                            constraintMatrix[leavingRowIndex][i] = new Var(constraintMatrix[leavingRowIndex][i].getValue() / pivot);
                        }
                        basicVariables[leavingRowIndex] = enteringColIndex;

                        for (int i = 0; i < rowLen; i++) {
                            if (i != leavingRowIndex) {
                                double multipleFactor = constraintMatrix[i][enteringColIndex].getValue() * -1;
                                for (int j = 0; j < colLen + 1; j++) {
                                    constraintMatrix[i][j] = new Var(constraintMatrix[i][j].getValue() + constraintMatrix[leavingRowIndex][j].getValue() * multipleFactor);
                                }
                            }
                        }
                    }
                }
            }

            iterationCount++;
        }
    }
}
