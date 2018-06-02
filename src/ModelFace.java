import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Group;

public class ModelFace
{
    private static final DoubleProperty ZERO_PROP = new SimpleDoubleProperty(0);
    private static final DoubleProperty HALF_ANGLE_PROP = new SimpleDoubleProperty(Math.PI/2);
    
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
    
    private static final double CHEEK_H = -.3;
    private static final double CHEEK_R = .8;

    // EYES
    private PropGroup eyesProp;
    private ModelSphere[] eyes;
    private ConvertBinder eyeAngle;
    private Binder eyeCorner;
    private ConvertBinder eyeCornerAngle;
    private Binder eyeInner;
    private ConvertBinder eyeInnerAngle;
    private OffsetBinder eyeBottom;
    private ConvertBinder eyeBottomAngle;
    
    // BROW
    private PropGroup browProp;
    private ModelBezier[] browOuter;
    private Binder outerBetween;
    
    // FOREHEAD
    private ModelBezier[] fhead;
    
    // JAW
    private final static int JAW_RES = 15;
    
    private PropGroup jawProp;
    private ModelParab chin;
    private ModelParab neckline;
    private ModelParab chinMedian;
    
    private Binder neckZeuth;
    private Binder midNeckZeuth;
    private Binder midChinLength;
    private Binder midChinHeight;
    private Binder neckFatness;
    
    private ModelBezier[] chinNeck;
    
    //  CHEEKS
    private PropGroup cheeksProp;
    private ModelBezier[] cheeks;
    
    // FUNCS
    
    public ModelFace(ModelPane mp)
    {
        initEyes(mp);
        initBrow(mp);
        //initForehead(mp);
        initJaw(mp);
        initCheeks(mp);
        
        Face.setShownPropGroup(eyesProp);
    }
    
    private void initEyes(ModelPane mp)
    {
        eyesProp = new PropGroup("Eyes");
        eyesProp.add("Spacing", EYES_X-.1, EYES_X+.1, EYES_X);
        eyesProp.add("Depth", EYES_Z-.1, EYES_Z+.1, EYES_Z);
        eyesProp.add("Size", EYES_R-.05, EYES_R+.05, EYES_R);
        eyesProp.add("Bottom", -Math.PI/2, -Math.PI/8, -Math.PI/4);
        
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
        
        eyeAngle = new ConvertBinder(eyesProp.valueProperty("Spacing"), eyesProp.valueProperty("Depth"));
        
        eyeCorner = new SumBinder(eyesProp.valueProperty("Spacing"), eyesProp.valueProperty("Size"));
        eyeCornerAngle = new ConvertBinder(eyeCorner.valueProperty(), eyesProp.valueProperty("Depth"));
        
        eyeInner = new DiffBinder(eyesProp.valueProperty("Spacing"), eyesProp.valueProperty("Size"));
        eyeInnerAngle = new ConvertBinder(eyeInner.valueProperty(), eyesProp.valueProperty("Depth"));
        
        eyeBottom = new OffsetBinder(eyesProp.valueProperty("Depth"),
                                     ZERO_PROP,
                                     eyesProp.valueProperty("Size"),
                                     eyesProp.valueProperty("Bottom"));
        eyeBottomAngle = new ConvertBinder(eyesProp.valueProperty("Spacing"), eyeBottom.getX());
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
        jawProp.add("Neckline Zeuth", -.10, 0);
        
        jawProp.add("Neck Fatness", -.1, .05);
        
        neckZeuth = new SumBinder(jawProp.valueProperty("Jaw Height"), jawProp.valueProperty("Neckline Zeuth"));
        midNeckZeuth = new MeanBinder(jawProp.valueProperty("Jaw Height"), neckZeuth.valueProperty());
        
        midChinLength = new MeanBinder(jawProp.valueProperty("Chin Length"), jawProp.valueProperty("Neckline Length"));
        midChinHeight = new MeanBinder(jawProp.valueProperty("Chin Height"), jawProp.valueProperty("Neckline Height"));
        
        neckFatness = new SumBinder(midChinLength.valueProperty(), jawProp.valueProperty("Neck Fatness"));
        
        chin = new ModelParab(JAW_RES,
                              ZERO_PROP,
                              jawProp.valueProperty("Jaw Height"),
                              ZERO_PROP,
                              jawProp.valueProperty("Jaw Width"),
                              jawProp.valueProperty("Chin Length"),
                              jawProp.valueProperty("Chin Height"));
        chin.setPropGroup(jawProp);
        mp.add(chin);
        
        neckline = new ModelParab(JAW_RES,
                                  ZERO_PROP,
                                  neckZeuth.valueProperty(),
                                  ZERO_PROP,
                                  jawProp.valueProperty("Neckline Width"),
                                  jawProp.valueProperty("Neckline Length"),
                                  jawProp.valueProperty("Neckline Height"));
        neckline.setPropGroup(jawProp);
        mp.add(neckline);
        
        chinMedian = new ModelParab(JAW_RES,
                                    ZERO_PROP,
                                    midNeckZeuth.valueProperty(),
                                    ZERO_PROP,
                                    jawProp.valueProperty("Jaw Width"),
                                    neckFatness.valueProperty(),
                                    midChinHeight.valueProperty());
        chinMedian.setPropGroup(jawProp);
        chinMedian.hideLines();
        mp.add(chinMedian);
        
        int cnSize = 9;
        int cnCenter = cnSize / 2;
        chinNeck = new ModelBezier[cnSize];
        
        for (int i = 0; i < cnSize; ++i)
        {
            chinNeck[i] = new ModelBezier(7, false);
            
            DoubleProperty angle = null;
            
            if (i == cnCenter)
            {
                angle = ZERO_PROP;
            }
            else if (i == cnCenter - 1 || i == cnCenter + 1)
            {
                angle = eyeInnerAngle.getTheta();
            }
            else if (i == cnCenter - 2 || i == cnCenter + 2)
            {
                angle = eyeBottomAngle.getTheta();
            }
            else if (i == cnCenter - 3 || i == cnCenter + 3)
            {
                angle = eyeCornerAngle.getTheta();
            }
            else if (i == cnCenter - 4 || i == cnCenter + 4)
            {
                angle = HALF_ANGLE_PROP;
            }
            
            chinNeck[i].addPoint(new CylCoord(chin.getRProp(angle),
                                          angle,
                                          chin.getZProp(angle)
                                          ));
            
            chinNeck[i].addPoint(new CylCoord(chinMedian.getRProp(angle),
                                          angle,
                                          chinMedian.getZProp(angle)
                                          ));
            
            chinNeck[i].addPoint(new CylCoord(neckline.getRProp(angle),
                                          angle,
                                          neckline.getZProp(angle)
                                          ));
            
            if (i < cnCenter)
                chinNeck[i].flip();
            
            mp.add(chinNeck[i]);
        }
    }
    
    private void initCheeks(ModelPane mp)
    {
        cheeksProp = new PropGroup("Cheeks");
        
        cheeksProp.add("Cheek Height (Inner)", CHEEK_H-.1, CHEEK_H+.1);
        cheeksProp.add("Cheek Depth (Inner)", CHEEK_R-.2, CHEEK_R+.2);
        
        cheeksProp.add("Cheek Height (Outer)", CHEEK_H, CHEEK_H+.2);
        cheeksProp.add("Cheek Depth (Outer)", CHEEK_R-.1, CHEEK_R+.1);
        
        int cSize = 6;
        int cCenter = cSize / 2;
        cheeks = new ModelBezier[cSize];
        
        for (int i = 0; i < cSize; ++i)
        {
            cheeks[i] = new ModelBezier(14);
            
            DoubleProperty angle = null;
            
            if (i == cCenter - 1 || i == cCenter)
            {
                angle = eyeInnerAngle.getTheta();
                
                cheeks[i].addPoint(new Coord(eyeInner.valueProperty(),
                                             ZERO_PROP,
                                             eyesProp.valueProperty("Depth")
                                             ));
            }
            else if (i == cCenter - 2 || i == cCenter + 1)
            {
                angle = eyeBottomAngle.getTheta();
                
                cheeks[i].addPoint(new Coord(eyesProp.valueProperty("Spacing"),
                                             eyeBottom.getY(),
                                             eyeBottom.getX()
                                             ));
                
                cheeks[i].addPoint(new CylCoord(cheeksProp.valueProperty("Cheek Depth (Inner)"),
                                                angle,
                                                cheeksProp.valueProperty("Cheek Height (Inner)")
                                                ));
            }
            else if (i == cCenter - 3 || i == cCenter + 2)
            {
                angle = eyeCornerAngle.getTheta();
                
                cheeks[i].addPoint(new Coord(eyeCorner.valueProperty(),
                                             ZERO_PROP,
                                             eyesProp.valueProperty("Depth")
                                             ));
                
                cheeks[i].addPoint(new CylCoord(cheeksProp.valueProperty("Cheek Depth (Outer)"),
                                                angle,
                                                cheeksProp.valueProperty("Cheek Height (Outer)")
                                                ));
            }
            
            cheeks[i].addPoint(new CylCoord(chin.getRProp(angle),
                                            angle,
                                            chin.getZProp(angle)
                                            ));
            
            if (i < cCenter)
                cheeks[i].flip();
            
            mp.add(cheeks[i]);
            
            cheeks[i].setPropGroup(cheeksProp);
        }
        
    }
}