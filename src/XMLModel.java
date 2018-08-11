import expparse.ExpNode;
import expparse.ExpParse;

import xmlparse.XMLStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class XMLModel
{
    private HashMap<String, PropGroup> pgroups;
    private HashMap<String, DoubleProperty> props;
    private ArrayList<Binder> binders;
    private ArrayList<ConvertBinder> angles;
    private HashMap<Double, DoubleProperty> constProps;
    private HashMap<String, ModelParab> parabs;

    private ModelPane mp;
    private String firstGroup;

    private static ExpParse parser;

    static
    {
        parser = new ExpParse();

        parser.addInfixOp(",", 0);

        parser.addInfixOp("+", 1);
        parser.addInfixOp("-", 1);

        parser.addInfixOp("*", 2);
        parser.addInfixOp("/", 2);

        parser.addPrefixOp("-", 3);

        parser.addPrefixOp("cos", 3);
        parser.addPrefixOp("sin", 3);

        parser.addPrefixOp("ParabR", 3);
        parser.addPrefixOp("ParabZ", 3);
    }

    public XMLModel(ModelPane _mp)
    {
        pgroups = new HashMap<String, PropGroup>();
        props = new HashMap<String, DoubleProperty>();
        binders = new ArrayList<Binder>();
        angles = new ArrayList<ConvertBinder>();
        constProps = new HashMap<Double, DoubleProperty>();
        parabs = new HashMap<String, ModelParab>();
        mp = _mp;
        firstGroup = null;
    }

    public XMLModel(ModelPane _mp, String fn)
    {
        this(_mp);
        try
        {
            XMLStruct xml = XMLStruct.parseFromFile(fn, false);
            load(xml);
        }
        catch (XMLStruct.BadSyntaxException e)
        {
            System.out.println("Failed to parse XML.");
            System.out.println(e);
        }

        if (firstGroup != null)
        {
            Face.setShownPropGroup(pgroups.get(firstGroup));
        }
    }

    public boolean isChanged()
    {
        for (Map.Entry<String, PropGroup> entry : pgroups.entrySet())
        {
            PropGroup pg = entry.getValue();

            if (pg.isChanged())
                return true;
        }

        return false;
    }

    public void resetChange()
    {
        for (Map.Entry<String, PropGroup> entry : pgroups.entrySet())
        {
            PropGroup pg = entry.getValue();
            pg.resetChange();
        }
    }

    public void reset()
    {
        for (Map.Entry<String, PropGroup> entry : pgroups.entrySet())
        {
            PropGroup pg = entry.getValue();

            for (int i = 0; i < pg.getNumProps(); ++i)
            {
                Prop p = pg.getProp(i);

                p.setToDefault();
            }

            pg.resetChange();
        }
    }

    // Load functions

    public void load(XMLStruct xml)
    {
        load(xml, null);
    }
    private void load(XMLStruct xml, String pgroup)
    {
        if (xml.getName().equals("section"))
        {
            pgroup = xml.getAttribValueFromName("pgroup");
        }

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct tag = xml.getChildElement(i);
            if (tag != null)
            {
                if (tag.getName().equals("pgroup"))
                {
                    loadPgroup(tag);
                }
                else if (tag.getName().equals("section"))
                {
                    load(tag);
                }
                else if (tag.getName().equals("for"))
                {
                    loadFor(tag, pgroup);
                }
                else if (tag.getName().equals("val"))
                {
                    loadVal(tag, pgroup);
                }
                else
                {
                    loadShape(tag, pgroup);
                }
            }
        }
    }

    private void loadFor(XMLStruct xml, String pgroup)
    {
        XMLStruct range = xml.getChildElement("range");

        ForRange fr = loadRange(range);

        boolean empty = false;
        while (!empty)
        {
            for (int i = 0; i < fr.numNames(); ++i)
            {
                String name = fr.getName(i);
                String val = fr.pop(name);

                if (val == null)
                {
                    empty = true;
                    break;
                }

                try
                {
                    DoubleProperty dp = resolveProp(val, null);
                    props.put(name, dp);
                }
                catch (BadValueException e)
                {
                    empty = true;
                    break;
                }
            }

            load(xml, pgroup);
        }
    }

    private ForRange loadRange(XMLStruct xml)
    {
        ForRange fr = new ForRange();

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct var = xml.getChildElement(i);
            if (var != null && var.getName().equals("var"))
            {
                String name = var.getAttribValueFromName("name");
                String value = var.getAttribValueFromName("value");

                if (name != null && value != null)
                {
                    fr.push(name, value);
                }
            }
        }

        return fr;
    }

    private static class ForRange
    {
        private ArrayList<String> names;
        private HashMap<String, LinkedList<String>> vals;

        public ForRange()
        {
            names = new ArrayList<String>();
            vals = new HashMap<String, LinkedList<String>>();
        }

        public ForRange push(String name, String val)
        {
            LinkedList<String> q = vals.getOrDefault(name, null);

            if (q == null)
            {
                vals.put(name, new LinkedList<String>());
                names.add(name);
            }

            vals.get(name).addLast(val);
            return this;
        }

        public String pop(String name)
        {
            LinkedList<String> q = vals.getOrDefault(name, null);
            if (q == null || q.size() == 0)
                return null;
            else
                return vals.get(name).removeFirst();
        }

        public String getName(int index)
        {
            if (index < 0 || index >= names.size())
                return null;
            else
                return names.get(index);
        }

        public int numNames()
        {
            return names.size();
        }
    }

    private void loadPgroup(XMLStruct xml)
    {
        String groupName = xml.getAttribValueFromName("name");
        if (groupName == null)
            return;
        else if (!pgroups.containsKey(groupName))
        {
            pgroups.put(groupName, new PropGroup(groupName));
            if (firstGroup == null)
                firstGroup = groupName;
        }

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct prop = xml.getChildElement(i);
            if (prop != null)
            {
                String type = prop.getName();
                if (type.equals("prop"))
                {
                    loadProp(prop, groupName);
                }
                else if (type.equals("val"))
                {
                    loadVal(prop, groupName);
                }
                else if (type.equals("angleval"))
                {
                    loadAngleVal(prop, groupName);
                }
            }
        }
    }

    private void loadProp(XMLStruct xml, String pgroup)
    {
        String name = xml.getAttribValueFromName("name");

        String minStr = xmlAttribOrElement(xml, "min");
        double min = 0;
        try
        {
            min = interpretVal(minStr);
        }
        catch (BadValueException e)
        {
        }

        String maxStr = xmlAttribOrElement(xml, "max");
        double max = 0;
        try
        {
            max = interpretVal(maxStr);
        }
        catch (BadValueException e)
        {
        }

        String defStr = xmlAttribOrElement(xml, "default");
        double def = (min + max) / 2;
        try
        {
            def = interpretVal(defStr);
        }
        catch (BadValueException e)
        {
        }

        pgroups.get(pgroup).add(name, min, max, def);
        DoubleProperty dp = pgroups.get(pgroup).valueProperty(name);
        props.put(pgroup + "." + name, dp);
    }

    private void loadVal(XMLStruct xml, String pgroup)
    {
        String name = xml.getAttribValueFromName("name");

        String str = xml.getAttribValueFromName("value");
        if (str == null)
        {
            for (int i = 0; i < xml.getNumChildren(); ++i)
            {
                str = xml.getChildString(i).trim();
                if (str != null)
                    break;
            }
        }

        if (str != null)
        {
            try
            {
                DoubleProperty dp = resolveProp(str, pgroup);
                props.put(pgroup + "." + name, dp);
            }
            catch (BadValueException e)
            {
            }
        }
    }

    private void loadAngleVal(XMLStruct xml, String pgroup)
    {
        String name = xml.getAttribValueFromName("name");

        String xStr = xmlAttribOrElement(xml, "x");
        DoubleProperty x = null;
        try
        {
            x = resolveProp(xStr, pgroup);
        }
        catch (BadValueException e)
        {
            return;
        }

        String zStr = xmlAttribOrElement(xml, "z");
        DoubleProperty z = null;
        try
        {
            z = resolveProp(zStr, pgroup);
        }
        catch (BadValueException e)
        {
            return;
        }

        ConvertBinder cb = new ConvertBinder(x, z);
        angles.add(cb);
        props.put(pgroup + "." + name + ".radius", cb.getR());
        props.put(pgroup + "." + name + ".theta", cb.getTheta());
    }

    private void loadShape(XMLStruct xml, String pgroup)
    {
        String type = xml.getName();

        if (xml.getAttribValueFromName("pgroup") != null)
            pgroup = xml.getAttribValueFromName("pgroup");

        String reflect = xml.getAttribValueFromName("reflect");
        if (reflect == null)
            reflect = "false";

        String visible = xml.getAttribValueFromName("visible");
        if (visible == null)
            visible = "true";

        int l = (reflect.equals("both") || reflect.equals("true")) ? 0 : 1;
        int u = (reflect.equals("both") || reflect.equals("false")) ? 1 : 0;

        for (int i = l; i <= u; ++i)
        {
            ModelShape ms = null;

            if (type.equals("sphere"))
                ms = loadSphere(xml, pgroup);
            else if (type.equals("bezier"))
                ms = loadBezier(xml, pgroup);
            else if (type.equals("parab"))
                ms = loadParab(xml, pgroup);

            if (ms != null)
            {
                if (pgroups.containsKey(pgroup))
                    ms.setPropGroup(pgroups.get(pgroup));
                if (i == 0)
                    ms.flip();
                if (visible.equals("true"))
                    mp.add(ms);
            }
        }
    }

    private ModelSphere loadSphere(XMLStruct xml, String pgroup)
    {
        XMLStruct center = xml.getChildElement("center");
        if (center == null)
            return null;
        DoubleProperty x = loadNamedProp(center, "x", pgroup);
        DoubleProperty y = loadNamedProp(center, "y", pgroup);
        DoubleProperty z = loadNamedProp(center, "z", pgroup);

        XMLStruct radius = xml.getChildElement("radius");
        if (radius == null)
            return null;
        DoubleProperty r = loadSingleProp(radius, pgroup);

        ModelSphere ms = new ModelSphere(x, y, z, r);
        return ms;
    }

    private ModelBezier loadBezier(XMLStruct xml, String pgroup)
    {
        int res;
        try
        {
            res = Integer.parseInt(xml.getAttribValueFromName("res"));
        }
        catch (Exception e)
        {
            return null;
        }

        String clickable = xml.getAttribValueFromName("clickable");
        if (clickable == null)
            clickable = "true";

        ModelBezier mb = new ModelBezier(res, clickable.equals("true"));

        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct point = xml.getChildElement(i);
            if (point != null && point.getName().equals("point"))
            {
                Coord c = loadCoord(point, pgroup);
                if (c != null)
                {
                    mb.addPoint(c);
                }
            }
        }

        return mb;
    }

    private ModelParab loadParab(XMLStruct xml, String pgroup)
    {
        int res;
        try
        {
            res = Integer.parseInt(xml.getAttribValueFromName("res"));
        }
        catch (Exception e)
        {
            return null;
        }

        String nolines = xml.getAttribValueFromName("nolines");
        if (nolines == null)
            nolines = "false";

        XMLStruct center = xml.getChildElement("center");
        if (center == null)
            return null;
        DoubleProperty x = loadNamedProp(center, "x", pgroup);
        DoubleProperty y = loadNamedProp(center, "y", pgroup);
        DoubleProperty z = loadNamedProp(center, "z", pgroup);

        XMLStruct width = xml.getChildElement("width");
        if (width == null)
            return null;
        DoubleProperty w = loadSingleProp(width, pgroup);

        XMLStruct length = xml.getChildElement("length");
        if (length == null)
            return null;
        DoubleProperty l = loadSingleProp(length, pgroup);

        XMLStruct angle = xml.getChildElement("angle");
        if (angle == null)
            return null;
        DoubleProperty a = loadSingleProp(angle, pgroup);

        ModelParab mp = new ModelParab(res, x, y, z, w, l, a);

        if (xml.getAttribValueFromName("name") != null)
        {
            parabs.put(xml.getAttribValueFromName("name"), mp);
            System.out.println("Parab " + xml.getAttribValueFromName("name"));
        }

        if (nolines.equals("true"))
        {
            mp.hideLines();
        }

        return mp;
    }

    public void loadState(XMLStruct xml)
    {
        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct pgroup = xml.getChildElement(i);
            if (pgroup != null && pgroup.getName().equals("pgroup"))
            {
                String pgname = pgroup.getAttribValueFromName("name");

                if (pgname != null && pgroups.containsKey(pgname))
                {
                    for (int j = 0; j < pgroup.getNumChildren(); ++j)
                    {
                        XMLStruct prop = pgroup.getChildElement(j);
                        if (prop != null && prop.getName().equals("prop"))
                        {
                            String pname = prop.getAttribValueFromName("name");
                            Prop p = pgroups.get(pgname).getProp(pname);

                            if (p != null)
                            {
                                String content = prop.getChildString();
                                double value = 0;

                                try
                                {
                                    value = Double.parseDouble(content);
                                    p.valueProperty().setValue(value);
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void loadStateFromFile(String fn)
    {
        try
        {
            XMLStruct xml = XMLStruct.parseFromFile(fn, false);
            loadState(xml);
        }
        catch (XMLStruct.BadSyntaxException bse)
        {
            System.out.println("Unable to read XML file " + fn);
        }
    }

    // Helper load functions

    private DoubleProperty loadSingleProp(XMLStruct xml, String pgroup)
    {
        String attrib = xml.getAttribValueFromName("value");
        String element = xml.getChildString();
        String str = (element != null) ? element.trim() : attrib;

        if (str != null)
        {
            DoubleProperty dp = null;
            try
            {
                dp = resolveProp(str, pgroup);
            }
            catch (BadValueException e)
            {
            }

            return dp;
        }

        return null;
    }

    private DoubleProperty loadNamedProp(XMLStruct xml, String name, String pgroup)
    {
        String str = xmlAttribOrElement(xml, name);
        if (str != null)
        {
            DoubleProperty dp = null;
            try
            {
                dp = resolveProp(str, pgroup);
            }
            catch (BadValueException e)
            {
            }

            return dp;
        }

        return null;
    }

    private Coord loadCoord(XMLStruct xml, String pgroup)
    {
        boolean cyl = false;
        if (xml.getAttribValueFromName("type") != null &&
            xml.getAttribValueFromName("type").equals("cyl"))
            cyl = true;

        Coord c = null;

        if (!cyl)
        {
            DoubleProperty x = loadNamedProp(xml, "x", pgroup);
            DoubleProperty y = loadNamedProp(xml, "y", pgroup);
            DoubleProperty z = loadNamedProp(xml, "z", pgroup);

            if (x != null && y != null && z != null)
                c = new Coord(x,y,z);
        }
        else
        {
            DoubleProperty r = loadNamedProp(xml, "r", pgroup);
            DoubleProperty theta = loadNamedProp(xml, "theta", pgroup);
            DoubleProperty z = loadNamedProp(xml, "z", pgroup);

            if (r != null && theta != null && z != null)
                c = new CylCoord(r,theta,z);
        }

        return c;
    }

    // Non-static helpers

    private double interpretVal(String str) throws BadValueException
    {
        if (str == null)
            throw new BadValueException("Passed null string");

        System.out.println("Interpreting \"" + str + "\"");
        ExpNode exp = parser.parse(str);

        if (exp == null)
        {
            throw new BadValueException("Bad expression");
        }
        else
        {
            return interpretVal(exp);
        }
    }
    private double interpretVal(ExpNode exp) throws BadValueException
    {
        String op = exp.getVal();
        if (op.equals("+"))
        {
            double sum = 0;
            for (ExpNode child : exp)
                sum += interpretVal(child);
            return sum;
        }
        else if (op.equals("-"))
        {
            if (exp.getNumChildren() == 1)
            {
                return -interpretVal(exp.getChild());
            }
            else
            {
                double difference = 0;
                boolean first = true;
                for (ExpNode child : exp)
                {
                    if (first)
                    {
                        System.out.println("First is " + interpretVal(child));
                        difference = interpretVal(child);
                        first = false;
                    }
                    else
                    {
                        System.out.println("Subtract " + interpretVal(child));
                        difference -= interpretVal(child);
                    }
                }
                System.out.println("Result is " + difference);
                return difference;
            }
        }
        else if (op.equals("*"))
        {
            double product = 1;
            for (ExpNode child : exp)
                product *= interpretVal(child);
            return product;
        }
        else if (op.equals("/"))
        {
            double quotient = 1;
            boolean first = true;
            for (ExpNode child : exp)
            {
                if (first)
                {
                    quotient = interpretVal(child);
                    first = false;
                }
                else
                {
                    quotient /= interpretVal(child);
                }
            }
            return quotient;
        }
        else if (op.equals("cos"))
        {
            return Math.cos(interpretVal(exp.getChild()));
        }
        else if (op.equals("sin"))
        {
            return Math.sin(interpretVal(exp.getChild()));
        }
        else if (op.equals("PI"))
        {
            return Math.PI;
        }
        else
        {
            try
            {
                double val = Double.parseDouble(op);
                return val;
            }
            catch (Exception e)
            {
                throw new BadValueException("Unexpected token " + op);
            }
        }
    }

    private DoubleProperty resolveProp(String str, String context) throws BadValueException
    {
        System.out.println("Resolving \"" + str + "\"");
        ExpNode exp = parser.parse(str);

        if (exp == null)
        {
            throw new BadValueException("Bad expression");
        }
        else
        {
            return resolveProp(exp, context);
        }
    }
    private DoubleProperty resolveProp(ExpNode exp, String context) throws BadValueException
    {
        String op = exp.getVal();
        if (op.equals("+"))
        {
            if (exp.getNumChildren() == 2)
            {
                DoubleProperty lhs = resolveProp(exp.getChild(0), context);
                DoubleProperty rhs = resolveProp(exp.getChild(1), context);

                Binder b = new SumBinder(lhs, rhs);
                binders.add(b);
                return b.valueProperty();
            }
            else
            {
                throw new BadValueException("Improper number of args to " + op);
            }
        }
        else if (op.equals("-"))
        {
            if (exp.getNumChildren() == 2)
            {
                DoubleProperty lhs = resolveProp(exp.getChild(0), context);
                DoubleProperty rhs = resolveProp(exp.getChild(1), context);

                Binder b = new DiffBinder(lhs, rhs);
                binders.add(b);
                return b.valueProperty();
            }
            else if (exp.getNumChildren() == 1)
            {
                DoubleProperty arg = resolveProp(exp.getChild(), context);

                Binder b = new Binder(arg.multiply(-1));
                binders.add(b);
                return b.valueProperty();
            }
            else
            {
                throw new BadValueException("Improper number of args to " + op);
            }
        }
        else if (op.equals("*"))
        {
            if (exp.getNumChildren() == 2)
            {
                DoubleProperty lhs = resolveProp(exp.getChild(0), context);
                DoubleProperty rhs = resolveProp(exp.getChild(1), context);

                Binder b = new Binder(lhs.multiply(rhs));
                binders.add(b);
                return b.valueProperty();
            }
            else
            {
                throw new BadValueException("Improper number of args to " + op);
            }
        }
        else if (op.equals("/"))
        {
            if (exp.getNumChildren() == 2)
            {
                DoubleProperty lhs = resolveProp(exp.getChild(0), context);
                DoubleProperty rhs = resolveProp(exp.getChild(1), context);

                Binder b = new Binder(lhs.divide(rhs));
                binders.add(b);
                return b.valueProperty();
            }
            else
            {
                throw new BadValueException("Improper number of args to " + op);
            }
        }
        else if (op.equals("cos"))
        {
            throw new BadValueException("Unsupported operation " + op);
        }
        else if (op.equals("sin"))
        {
            throw new BadValueException("Unsupported operation " + op);
        }
        else if (op.indexOf("Parab") == 0)
        {
            if (exp.getNumChildren() == 1)
            {
                ExpNode argsExp = exp.getChild();

                if (argsExp.getVal().equals(",") &&
                    argsExp.getNumChildren() == 2)
                {
                    String parabName = argsExp.getChild(0).getVal();
                    DoubleProperty theta = resolveProp(argsExp.getChild(1), context);

                    if (!parabs.containsKey(parabName))
                        throw new BadValueException("Unrecognized parabola " + parabName);
                    ModelParab mp = parabs.get(parabName);
                    
                    if (op.charAt(5) == 'R')
                        return mp.getRProp(theta);
                    else if (op.charAt(5) == 'Z')
                        return mp.getZProp(theta);
                    else
                        throw new BadValueException("Unrecognized parab function " + op);
                }
                else
                {
                    throw new BadValueException("Improper number of args to " + op);
                }
            }
            else
            {
                throw new BadValueException("Improper number of args to " + op);
            }
        }
        else
        {
            DoubleProperty prop = null;

            try
            {
                prop = resolveSingleProp(op, context);
            }
            catch (BadValueException e)
            {
                prop = null;
            }

            if (prop == null)
            {
                double val = 0;

                if (op.equals("PI"))
                {
                    val = Math.PI;
                }
                else
                {
                    try
                    {
                        val = Double.parseDouble(op);
                    }
                    catch (Exception e)
                    {
                        throw new BadValueException("Unexpected token " + op);
                    }
                }

                if (constProps.containsKey(val))
                {
                    return constProps.get(val);
                }
                else
                {
                    prop = new SimpleDoubleProperty(val);
                    constProps.put(val, prop);
                }
            }

            return prop;
        }
    }

    private DoubleProperty resolveSingleProp(String str, String context) throws BadValueException
    {
        if (str == null)
            throw new BadValueException("");

        if (context == null)
            context = "";

        if (str.indexOf(".") == 0)
            throw new BadValueException(str);

        // First try without context
        if (props.containsKey(str))
            return props.get(str);

        // Add context
        if (context != null)
            str = context + "." + str;
        else
            throw new BadValueException(str);

        if (props.containsKey(str))
            return props.get(str);

        throw new BadValueException(str);
    }

    // Save functions

    public XMLStruct saveState()
    {
        XMLStruct root = new XMLStruct("data");

        for (Map.Entry<String, PropGroup> entry : pgroups.entrySet())
        {
            PropGroup pg = entry.getValue();

            XMLStruct pgxml = new XMLStruct("pgroup", "name", pg.getName());

            for (int i = 0; i < pg.getNumProps(); ++i)
            {
                Prop p = pg.getProp(i);
                String name = p.getName();
                double value = p.getValue();

                XMLStruct pxml = new XMLStruct("prop", "name", name);
                pxml.addChild("" + value);

                pgxml.addChild(pxml);
            }

            root.addChild(pgxml);
        }

        return root;
    }

    public String saveStateToFile(String fn)
    {
        XMLStruct xml = saveState();
        return xml.saveToFile(fn, true);
    }

    // Static helpers

    private static String xmlAttribOrElement(XMLStruct xml, String name)
    {
        String attrib = xml.getAttribValueFromName(name);
        XMLStruct element = xml.getChildElement(name);

        if (element == null || element.getChildString() == null)
        {
            if (attrib != null)
                return attrib.trim();
            else
                return null;
        }
        else
        {
            return element.getChildString().trim();
        }
    }

    private static int skipWS(String str, int i)
    {
        for (; i < str.length(); ++i)
        {
            char c = str.charAt(i);

            switch (c)
            {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    break;
                default:
                    return i;
            }
        }

        return i;
    }

    private static int skipUntil(String str, int i, char[] look)
    {
        if (look == null)
            return str.length();

        for (; i < str.length(); ++i)
        {
            char c = str.charAt(i);

            for (int j = 0; j < look.length; ++j)
            {
                if (c == look[j])
                    return i;
            }
        }

        return i;
    }

    // Other static members

    private static class BadValueException extends Exception
    {
        public BadValueException(String val)
        {
            super(val);
        }
    }

    private static char[] WS_OR_OP = {' ', '\n', '\r', '\t',
                                      '+', '-', '*', '/'};
}
