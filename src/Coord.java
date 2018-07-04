import javafx.beans.property.DoubleProperty;

public class Coord
{
    public static final int CTYPE_RECT = 0;
    public static final int CTYPE_CYL = 1;

    private int dims;
    private double[] n;

    private boolean useMirror;
    private int[] mirror;

    int type;

    private Runnable changeCallback;


    public Coord()
    {
        this(0,0,0);
    }

    public Coord(double ... _n)
    {
        dims = _n.length;
        n = new double[dims + 1];

        useMirror = false;
        mirror = new int[dims];

        type = CTYPE_RECT;

        for (int i = 0; i < dims; ++i)
        {
            n[i] = _n[i];
        }
        n[dims] = 1;

        for (int j = 0; j < dims; ++j)
        {
            mirror[j] = 1;
        }
    }

    public Coord(DoubleProperty ... _dp)
    {
        dims = _dp.length;
        n = new double[dims + 1];

        useMirror = false;
        mirror = new int[dims];

        type = CTYPE_RECT;

        for (int i = 0; i < dims; ++i)
        {
            bindDoubleProperty(i, _dp[i]);
        }
        n[dims] = 1;

        for (int j = 0; j < dims; ++j)
        {
            mirror[j] = 1;
        }
    }

    public void bindDoubleProperty(int dim, DoubleProperty _dp)
    {
        n[dim] = _dp.getValue();

        _dp.addListener(
                (o, oldVal, newVal) ->
                {
                    n[dim] = _dp.getValue();
                    if (changeCallback != null)
                        changeCallback.run();
                }
            );
    }

    public void setChangeCallback(Runnable r)
    {
        changeCallback = r;
    }

    public double[] getValue()
    {
        if (!useMirror)
        {
            if (type == CTYPE_CYL)
                return Matrix.cylToRect(n);
            else
                return n;
        }
        else
        {
            double[] mirroredVal = new double[dims + 1];

            double[] rect = (type == CTYPE_CYL) ? Matrix.cylToRect(n) : n;

            for (int i = 0; i < dims; ++i)
            {
                mirroredVal[i] = rect[i] * mirror[i];
            }

            mirroredVal[dims] = 1;

            return mirroredVal;
        }
    }

    public void setMirror(int ... _mirror)
    //throws Exception
    {
        //if (_mirror.length != mirror.length)
            //throw new Exception("Incorrect number of arguments");

        useMirror = true;

        for (int i = 0; i < dims; ++i)
        {
            mirror[i] = _mirror[i];
        }
    }

    public void setType(int _type)
    {
        type = _type;
    }

    public String toString()
    {
        String str = "(";
        double[] val = getValue();
        for (int i = 0; i < val.length - 1; ++i)
        {
            str += val[i];
            if (i < val.length - 2)
                str += ", ";
        }
        str += ")";
        return str;
    }
}
