import xmlparse.XMLStruct;

import java.util.HashMap;
import javafx.beans.property.DoubleProperty;

public class XMLModel
{
    private HashMap<String, PropGroup> pgroups;
    private HashMap<String, DoubleProperty> props;
    private ModelPane mp;
    private String firstGroup;

    public XMLModel(ModelPane _mp)
    {
        pgroups = new HashMap<String, PropGroup>();
        props = new HashMap<String, DoubleProperty>();
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
        catch (Exception e)
        {
            System.out.println("Failed to parse XML.");
            System.out.println(e);
        }

        if (firstGroup != null)
        {
            Face.setShownPropGroup(pgroups.get(firstGroup));
        }
    }

    public void load(XMLStruct xml)
    {
        for (int i = 0; i < xml.getNumChildren(); ++i)
        {
            XMLStruct section = xml.getChildElement(i);
            if (section != null)
            {
                if (section.getName().equals("pgroup"))
                {
                    loadPgroup(section);
                }
                else if (section.getName().equals("section"))
                {
                    load(section);
                }
                else
                {
                    loadShape(section);
                }
            }
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
            }
        }
    }

    private void loadProp(XMLStruct xml, String pgroup)
    {
        String name = xmlAttribOrElement(xml, "name");

        String minStr = xmlAttribOrElement(xml, "min");
        double min = 0;
        try
        {
            min = interpretVal(minStr);
        }
        catch (Exception e)
        {
        }

        String maxStr = xmlAttribOrElement(xml, "max");
        double max = 0;
        try
        {
            max = interpretVal(maxStr);
        }
        catch (Exception e)
        {
        }

        String defStr = xmlAttribOrElement(xml, "default");
        double def = (min + max) / 2;
        try
        {
            def = interpretVal(defStr);
        }
        catch (Exception e)
        {
        }

        pgroups.get(pgroup).add(name, min, max, def);
    }

    private void loadShape(XMLStruct xml)
    {

    }

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

    private double interpretVal(String str) throws BadValueException
    {
        int[] i = new int[1];
        i[0] = 0;
        double val = interpretVal(str, i);
        System.out.println("[" + val + "]");
        return val;
    }
    private double interpretVal(String str, int[] i) throws BadValueException
    {
        if (str == null)
            throw new BadValueException(str);

        double val = 0;
        char op = '+';
        boolean first = true;

        while (i[0] < str.length())
        {
            int j = skipWS(str, i[0]);
            i[0] = skipUntil(str, j, WS_OR_OP);

            if (i[0] == j)  // single operator
                ++i[0];

            String found = str.substring(j, i[0]);

            if (op == 0)
            {
                // Get the operation to use
                if (found.length() > 1)
                    throw new BadValueException(str);

                op = found.charAt(0);
            }
            else
            {
                double foundVal = 0;
                if ((op == '+' || op == '-') && !first)
                {
                    i[0] = j;
                    foundVal = interpretVal(str, i);
                }
                else
                {
                    if (found.charAt(0) == '-')
                    {
                        j = i[0];
                        i[0] = skipUntil(str, j, WS_OR_OP);
                        found = "-" + str.substring(j, i[0]);
                    }

                    if (found.equals("PI"))
                        foundVal = Math.PI;
                    if (found.equals("-PI"))
                        foundVal = -Math.PI;
                    else
                    {
                        try
                        {
                            foundVal = Double.parseDouble(found);
                        }
                        catch (Exception e)
                        {
                            throw new BadValueException(str);
                        }
                    }
                }

                if (op == '+')
                    val += foundVal;
                else if (op == '-')
                    val -= foundVal;
                else if (op == '*')
                    val *= foundVal;
                else if (op == '/')
                    val /= foundVal;
                else
                    throw new BadValueException(str);

                op = 0;
            }

            first = false;
        }

        return val;
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
