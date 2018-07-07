public final class Matrix
{
    static double[][] add(double[][] m1, double[][] m2)
    throws ArithmeticException
    {
        if (m1.length != m2.length || m1[0].length != m2[0].length)
            throw new ArithmeticException("Invalid matrix dimensions");

        int rows = m1.length;
        int cols = m1[0].length;

        double[][] m3 = new double[rows][cols];

        for (int r = 0; r < rows; ++r)
        {
            for (int c = 0; c < cols; ++c)
            {
                m3[r][c] = m1[r][c] + m2[r][c];
            }
        }

        return m3;
    }

    static double[][] mult(double[][] m1, double[][] m2)
    throws ArithmeticException
    {
        if (m1[0].length != m2.length)
            throw new ArithmeticException("Invalid matrix dimensions");

        int inner = m1[0].length;
        int rows = m1.length;
        int cols = m2[0].length;

        double[][] m3 = new double[rows][cols];

        for (int r = 0; r < rows; ++r)
        {
            for (int c = 0; c < cols; ++c)
            {
                for (int i = 0; i < inner; ++i)
                {
                    m3[r][c] += m1[r][i] * m2[i][c];
                }
            }
        }

        return m3;
    }

    static double[][] scale(double[][] mat, double scalar)
    {
        int rows = mat.length;
        int cols = mat[0].length;

        double[][] newMat = new double[rows][cols];

        for (int r = 0; r < rows; ++r)
        {
            for (int c = 0; c < cols; ++c)
            {
                newMat[r][c] = mat[r][c] * scalar;
            }
        }

        return newMat;
    }

    static double[] tf(double[][] mat, double[] vec)
    throws ArithmeticException
    {
        if (mat[0].length != vec.length)
            throw new ArithmeticException("Invalid matrix dimensions");

        double[][] colMat = columnize(vec);

        colMat = mult(mat, colMat);

        double[] newVec = vectorize(colMat);

        return newVec;
    }

    static double[][] rotX(double theta)
    {
        double[][] mat = new double[4][4];

        mat[0][0] = 1;

        mat[1][1] = Math.cos(theta);
        mat[2][2] = Math.cos(theta);

        mat[1][2] = -Math.sin(theta);
        mat[2][1] = Math.sin(theta);

        mat[3][3] = 1;

        return mat;
    }

    static double[][] rotY(double theta)
    {
        double[][] mat = new double[4][4];

        mat[1][1] = 1;

        mat[0][0] = Math.cos(theta);
        mat[2][2] = Math.cos(theta);

        mat[2][0] = -Math.sin(theta);
        mat[0][2] = Math.sin(theta);

        mat[3][3] = 1;

        return mat;
    }

    static double[][] rotZ(double theta)
    {
        double[][] mat = new double[4][4];

        mat[2][2] = 1;

        mat[0][0] = Math.cos(theta);
        mat[1][1] = Math.cos(theta);

        mat[0][1] = -Math.sin(theta);
        mat[1][0] = Math.sin(theta);

        mat[3][3] = 1;

        return mat;
    }

    static double[][] dil(double x, double y, double z)
    {
        double[][] mat = new double[4][4];

        mat[0][0] = x;
        mat[1][1] = y;
        mat[2][2] = z;
        mat[3][3] = 1;

        return mat;
    }

    static double[][] yawPitch(double yaw, double pitch)
    {
        return mult(rotY(yaw), rotX(pitch));
    }

    static double[][] tl(double dx, double dy, double dz)
    {
        double[][] mat = Matrix.ident(4);

        mat[0][3] = dx;
        mat[1][3] = dy;
        mat[2][3] = dz;

        return mat;
    }

    static double[][] tp(double[][] mat)
    {
        int oldRows = mat.length;
        int oldCols = mat[0].length;

        double[][] newMat = new double[oldCols][oldRows];

        for (int r = 0; r < oldCols; ++r)
        {
            for (int c = 0; c < oldRows; ++c)
            {
                newMat[r][c] = mat[c][r];
            }
        }

        return newMat;
    }

    static double[][] concat(double[] ... vecs)
    {
        return tp(vecs);
    }

    static double[][] columnize(double[] vec)
    {
        double[][] colMat = new double[vec.length][1];

        for (int r = 0; r < vec.length; ++r)
        {
            colMat[r][0] = vec[r];
        }

        return colMat;
    }

    static double[][] makeColVec(double ... vec)
    {
        return columnize(vec);
    }

    static double[] vectorize(double[][] mat)
    throws ArithmeticException
    {
        if (mat[0].length != 1)
            throw new ArithmeticException("Vectorizing non-column matrix");

        double[] vec = new double[mat.length];

        for (int r = 0; r < mat.length; ++r)
        {
            vec[r] = mat[r][0];
        }

        return vec;
    }

    static double[][] ident(int dim)
    {
        double[][] mat = new double[dim][dim];

        for (int i = 0; i < dim; ++i)
        {
            mat[i][i] = 1;
        }

        return mat;
    }

    public static double[] rectToCyl(double[] vec)
    {
        double[] newVec = new double[vec.length];

        newVec[0] = Math.sqrt(vec[0]*vec[0] + vec[2]*vec[2]);

        double theta = Math.atan2(vec[0], vec[2]);
        while (theta < 0)
            theta += 2 * Math.PI;
        newVec[1] = theta;

        newVec[2] = vec[1];

        for (int i = 3; i < vec.length; ++i)
            newVec[i] = vec[i];

        return newVec;
    }

    public static double[] cylToRect(double[] vec)
    {
        double[] newVec = new double[vec.length];

        newVec[0] = vec[0] * Math.sin(vec[1]);
        newVec[1] = vec[2];
        newVec[2] = vec[0] * Math.cos(vec[1]);

        for (int i = 3; i < vec.length; ++i)
            newVec[i] = vec[i];

        return newVec;
    }

    private static void sop(String s)
    {
        System.out.print(s);
    }

    public static void printMat(double[][] mat)
    {
        sop("[");
        for (int r = 0; r < mat.length; ++r)
        {
            if  (r != 0)
                sop(" ");

            for (int c = 0; c < mat[0].length; ++c)
            {
                sop("" + mat[r][c]);
                if (c < mat[0].length - 1)
                    sop(" ");
            }
            if (r == mat.length - 1)
                sop("]");
            sop("\n");
        }
    }

    public static void printVec(double[] vec)
    {
        sop("<");
        for (int r = 0; r < vec.length; ++r)
        {
            sop("" + vec[r]);
            if (r < vec.length - 1)
                sop(", ");
        }
        sop(">\n");
    }
}
