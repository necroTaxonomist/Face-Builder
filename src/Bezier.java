public final class Bezier
{
    public static double[] bezier(double t, double[] ... points)
    {
        //System.out.println("" + (points.length - 1) + "th degree Bezier curve");
        //System.out.println("" + points[0].length + " dimensions");
        //System.out.println("at t = " + t);
        
        int degree = points.length - 1;
        
        double[][] x = Matrix.tp(points);
        double[][] c = coeffs(degree, t);
        double[][] b = Matrix.mult(x, c);
        
        return Matrix.vectorize(b);
    }
    
    public static double[][] bezier(double t[], double[][] points)
    {
        //System.out.println("" + (points[0].length - 1) + "th degree Bezier curve");
        //System.out.println("" + points.length + " dimensions");
        //System.out.print("at t = ");
        //Matrix.printVec(t);
        
        int degree = points[0].length - 1;
        int numPoints = t.length;
        
        double[][] c = new double[degree + 1][numPoints];
        
        for (int i = 0; i < numPoints; ++i)
        {
            double[][] col = coeffs(degree, t[i]);
            
            for (int r = 0; r < degree + 1; ++r)
            {
                c[r][i] = col[r][0];
            }
        }
        
        double[][] b = Matrix.mult(points, c);
        
        return b;
    }
    
    private static double[][] coeffs(int degree, double t)
    {
        double A = 1 - t;
        double B = t;
        
        double[][] f1 = {{A}, {B}};  // 2 x 1 matrix, changes
        double[][] f2 = {{A, B}};  // 1 x 2 matrix, stays the same
        
        for (int rows = 2; rows <= degree; ++rows)
        {
            double[][] p = Matrix.mult(f1, f2);
            
            f1 = new double[rows + 1][1];
            
            f1[0][0] = p[0][0];
            for (int r = 1; r < rows; ++r)
            {
                f1[r][0] = p[r][0] + p[r-1][1];
            }
            f1[rows][0] = p[rows - 1][1];
        }
        
        return f1;
    }
    
}