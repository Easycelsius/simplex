package com;

public class CaseExample01 {
    public static void main(String[] args) {
        Object[] objectiveFunction = {2, 1, 3};
        Object[][] constraintMatrix = {
                {1, 1, 1, "<=", 20},
                {2, 3, 1, "<=", 30},
                {5, 2, 6, "<=", 50},
        };

        SimplexTableau simplexTableau = new SimplexTableau(objectiveFunction, constraintMatrix, SimplexSolveType.MAXIMIZE);
        simplexTableau.solve();
    }
}
