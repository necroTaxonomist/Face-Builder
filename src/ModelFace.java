import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Group;

public class ModelFace
{
    private static final DoubleProperty ZERO_PROP = new SimpleDoubleProperty(0);
    
    // BASE MEASUREMENTS
    private static final double HEAD_TOP_Y = 1;
    
    private static final double EYES_X = .3;
    private static final double EYES_Z = .6;
    private static final double EYES_R = .15;
    
    private static final double BROW_OUTER_R = .8;
    private static final double BROW_OUTER_Z = .15;
    
    private static final double HEAD_W = 1.3;
    
    private static final double JAW_W = 1.0;
    private static final double JAW_Y = -.6;
    
    private static final double CHIN_L = .8;
    private static final double CHIN_ANGLE = .46;
    
    private static final double NECK_W = 0.9;
    private static final double NECK_L = .40;
    private static final double NECK_ANGLE = .9;

    // EYES
    private PropGroup eyesProp;
    private ModelSphere[] eyes;
    private Binder eyeCorner;
    private ConvertBinder eyeCornerAngle;
    
    // BROW
    private PropGroup browProp;
    private ModelBezier[] browOuter;
    private Binder outerBetween;
    
    // FOREHEAD
    private ModelBezier[] fhead;
    
    // JAW
    private PropGroup jawProp;
    private ModelParab chin;
    private ModelParab neckline;
    
    // FUNCS
    
    public ModelFace(ModelPane mp)
    {
        initEyes(mp);
        initBrow(mp);
        //initForehead(mp);
        initJaw(mp);
        
        Face.setShownPropGroup(eyesProp);
    }
    
    private void initEyes(ModelPane mp)
    {
        eyesProp = new PropGroup("Eyes");
        eyesProp.add("Spacing", EYES_X-.1, EYES_X+.1, EYES_X);
        eyesProp.add("Depth", EYES_Z-.1, EYES_Z+.1, EYES_Z);
        eyesProp.add("Size", EYES_R-.05, EYES_R+.05, EYES_R);
        
        eyes = new ModelSphere[2];
        for (int i = 0; i < 2; ++i)
        {
            eyes[i] = new ModelSphere(eyesProp.valueProperty("Spacing"),
                                      ZERO_PROP,
                                      eyesProp.valueProperty("Depth"),
                                      eyesProp.valueProperty("Size"));
            eyes[i].setPropGroup(eyesProp);
            if (i == 0)
                eyes[i].flip();
            mp.add(eyes[i]);
        }
        
        eyeCorner = new Binder(eyesProp.valueProperty("Spacing").add(eyesProp.valueProperty("Size")));
        eyeCornerAngle = new ConvertBinder(eyeCorner.valueProperty(), eyesProp.valueProperty("Depth"));
    }
    
    private void initBrow(ModelPane mp)
    {
        browProp = new PropGroup("Brow");
        browProp.add("Outer Depth", BROW_OUTER_R-.1, BROW_OUTER_R+.1, BROW_OUTER_R);
        browProp.add("Outer Height", BROW_OUTER_Z-.05, BROW_OUTER_Z+.05, BROW_OUTER_Z);
        
        outerBetween = new Binder(browProp.valueProperty("Outer Height").multiply(.5));
        
        browOuter = new ModelBezier[2];
        for (int i = 0; i < 2; ++i)
        {
            browOuter[i] = new ModelBezier(7);
            
            browOuter[i].addPoint(new Coord(eyeCorner.valueProperty(),
                                            ZERO_PROP,
                                            eyesProp.valueProperty("Depth")));
            browOuter[i].addPoint(new CylCoord(browProp.valueProperty("Outer Depth"),
                                               eyeCornerAngle.getTheta(),
                                               outerBetween.valueProperty()));
            browOuter[i].addPoint(new CylCoord(browProp.valueProperty("Outer Depth"),
                                               eyeCornerAngle.getTheta(),
                                               browProp.valueProperty("Outer Height")));
            browOuter[i].setPropGroup(browProp);
            if (i == 0)
                browOuter[i].flip();
            mp.add(browOuter[i]);
        }
    }
    
    private void initForehead(ModelPane mp)
    {
        fhead = new ModelBezier[9];
        
        for (int i = 0; i < 9; ++i)
        {
            if (i == 2 || i == 7)
            {
                fhead[i] = new ModelBezier(20);
                fhead[i].addPoint(new CylCoord(browProp.valueProperty("Outer Depth"),
                                               eyeCornerAngle.getTheta(),
                                               browProp.valueProperty("Outer Height")));
                fhead[i].addPoint(new Coord(ZERO_PROP,
                                            new SimpleDoubleProperty(HEAD_TOP_Y),
                                            ZERO_PROP));
                if (i == 2)
                    fhead[i].flip();
                mp.add(fhead[i]);
            }
        }
    }
    
    private void initJaw(ModelPane mp)
    {
        jawProp = new PropGroup("Jaw + Chin");
        jawProp.add("Jaw Width", JAW_W-.2, JAW_W+.2);
        jawProp.add("Jaw Height", JAW_Y-.1, JAW_Y+.1);
        
        jawProp.add("Chin Length", CHIN_L-.1, CHIN_L+.2, CHIN_L);
        jawProp.add("Chin Height", CHIN_ANGLE-.2 + Math.PI/2, CHIN_ANGLE+.2 + Math.PI/2);
        
        jawProp.add("Neckline Width", NECK_W-.2, NECK_W+.2);
        jawProp.add("Neckline Length", NECK_L-.1, NECK_L+.2, NECK_L);
        jawProp.add("Neckline Height", NECK_ANGLE-.2 + Math.PI/2, NECK_ANGLE+.2 + Math.PI/2);
        
        chin = new ModelParab(15,
                              ZERO_PROP,
                              jawProp.valueProperty("Jaw Height"),
                              ZERO_PROP,
                              jawProp.valueProperty("Jaw Width"),
                              jawProp.valueProperty("Chin Length"),
                              jawProp.valueProperty("Chin Height"));
        chin.setPropGroup(jawProp);
        mp.add(chin);
        
        neckline = new ModelParab(15,
                                  ZERO_PROP,
                                  jawProp.valueProperty("Jaw Height"),
                                  ZERO_PROP,
                                  jawProp.valueProperty("Neckline Width"),
                                  jawProp.valueProperty("Neckline Length"),
                                  jawProp.valueProperty("Neckline Height"));
        neckline.setPropGroup(jawProp);
        mp.add(neckline);
    }
}