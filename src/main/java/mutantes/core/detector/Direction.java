package mutantes.core.detector;

enum Direction {
    HORIZONTAL(
            new int[]{0, 1},
            new StartEnumerator() {
                @Override
                public int[] start(int nc) {
                    return new int[]{0, 0};
                }

                @Override
                public int[] next(int i, int j, int nc, int nr) {
                    if (i < (nr - 1))
                        return new int[]{i + 1, j};

                    return null;
                }
            }),
    VERTICAL(
            new int[]{1, 0},
            new StartEnumerator() {
                @Override
                public int[] start(int nc) {
                    return new int[]{0, 0};
                }

                @Override
                public int[] next(int i, int j, int nc, int nr) {
                    if (j < (nc - 1))
                        return new int[]{i, j + 1};
                    return null;
                }
            }
    ),
    DIAGONAL(
            new int[]{1, 1},
            new StartEnumerator() {
                @Override
                public int[] start(int nc) {
                    return new int[]{0, nc - 1};
                }

                @Override
                public int[] next(int i, int j, int nc, int nr) {
                    if (j > 0)
                        return new int[]{i, j - 1};

                    if (i < (nr - 1))
                        return new int[]{i + 1, j};

                    return null;
                }
            }
    ),
    IDIAGONAL(
            new int[]{-1, 1},
            new StartEnumerator() {
                @Override
                public int[] start(int nc) {
                    return new int[]{0, 0};
                }

                @Override
                public int[] next(int i, int j, int nc, int nr) {
                    if (i < (nr - 1))
                        return new int[]{i + 1, j};

                    if (j < (nc - 1))
                        return new int[]{i, j + 1};

                    return null;
                }
            }
    );

    int[] moveDirection;
    StartEnumerator startEnum;

    /**
     * @param moveDirection what needs to be added to i and j to move in the corresponding direction
     * @param startEnum
     */
    Direction(int[] moveDirection, StartEnumerator startEnum) {
        this.moveDirection = moveDirection;
        this.startEnum = startEnum;
    }
}
