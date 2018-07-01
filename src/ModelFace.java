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

    private static final double MOUTH_H = -.7;
    private static final double MOUTH_D = .675;
    private static final double BITE = .05;

    private static final double ULIP_L = .025;
    private static final double ULIP_ANGLE = .25;
    private static final double LLIP_L = .015;

    private static final double CHIN_IN = .2;
    private static final double CHIN_OUT = .2;
    private static final double CHIN_SIDE = .05;

    private static final double NOSE_W = .3;
    private static final double NOSE_Y = -.5;
    private static final double NOSE_Z = .7;
    private static final double NOSE_L = .15;

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

    // CHEEKS
    private PropGroup cheeksProp;
    private ModelBezier[] cheeks;

    // MOUTH
    private PropGroup mouthProp;
    private ModelParab mouth;
    private ModelParab upperLip;
    private ModelParab lowerLip;

    Binder ulipAdd;
    Binder llipAdd;

    // CHIN
    private PropGroup chinProp;

    private ModelBezier chinFront;
    private ModelBezier[] chinSide;

    private Binder chinLipDist;
    private Binder clDist1;
    private Binder clDist2;
    private Binder chinH1;
    private Binder chinH2;
    private Binder chinD1;
    private Binder chinD2;

    private Binder halfMouthWidth;
    private ConvertBinder mouthCornerAngle;

    private Binder clDistSide;
    private Binder clDist3;
    private Binder chinH3;
    private Binder chinD3;

    // NOSE
    private PropGroup noseProp;

    private ModelParab nose;

    private ModelBezier[] noseBottom;
    private ModelBezier[] noseLines;
    private ModelBezier[] septum;

    private Binder noseLowest;
    private Binder noseLowestFront;
    private Binder halfNoseWidth;
    private Binder noseLineCurve;

    // FUNCS

    public ModelFace(ModelPane mp)
    {
        initEyes(mp);
        initBrow(mp);
        //initForehead(mp);
        initJaw(mp);
        initCheeks(mp);
        initMouth(mp);
        initChin(mp);
        initNose(mp);

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

    private void initMouth(ModelPane mp)
    {
        mouthProp = new PropGroup("Mouth");

        mouthProp.add("Mouth Height", MOUTH_H-.1, MOUTH_H+.1);
        mouthProp.add("Mouth Width", EYES_X * 2 - .4, EYES_X * 2);
        mouthProp.add("Mouth Depth", MOUTH_D-.1, MOUTH_D+.1);
        mouthProp.add("Bite Size", 0, BITE+.1);

        mouthProp.add("Upper Lip Size",ULIP_L-.02, ULIP_L+.04);
        mouthProp.add("Upper Lip Height",Math.PI/2-ULIP_ANGLE-.4, Math.PI/2-ULIP_ANGLE+.1);

        mouthProp.add("Lower Lip Size",LLIP_L-.02, LLIP_L+.04);
        mouthProp.add("Lower Lip Height",Math.PI/2+ULIP_ANGLE-.1, Math.PI/2+ULIP_ANGLE+.4);

        mouth = new ModelParab(7,
                               ZERO_PROP,
                               mouthProp.valueProperty("Mouth Height"),
                               mouthProp.valueProperty("Mouth Depth"),
                               mouthProp.valueProperty("Mouth Width"),
                               mouthProp.valueProperty("Bite Size"),
                               HALF_ANGLE_PROP);
        mp.add(mouth);
        mouth.setPropGroup(mouthProp);

        ulipAdd = new SumBinder(mouthProp.valueProperty("Bite Size"), mouthProp.valueProperty("Upper Lip Size"));

        upperLip = new ModelParab(7,
                                  ZERO_PROP,
                                  mouthProp.valueProperty("Mouth Height"),
                                  mouthProp.valueProperty("Mouth Depth"),
                                  mouthProp.valueProperty("Mouth Width"),
                                  ulipAdd.valueProperty(),
                                  mouthProp.valueProperty("Upper Lip Height"));
        mp.add(upperLip);
        upperLip.setPropGroup(mouthProp);

        llipAdd = new SumBinder(mouthProp.valueProperty("Bite Size"), mouthProp.valueProperty("Lower Lip Size"));

        lowerLip = new ModelParab(7,
                                  ZERO_PROP,
                                  mouthProp.valueProperty("Mouth Height"),
                                  mouthProp.valueProperty("Mouth Depth"),
                                  mouthProp.valueProperty("Mouth Width"),
                                  llipAdd.valueProperty(),
                                  mouthProp.valueProperty("Lower Lip Height"));
        mp.add(lowerLip);
        lowerLip.setPropGroup(mouthProp);
    }

    private void initChin(ModelPane mp)
    {
        chinProp = new PropGroup("Chin");

        chinProp.add("Outer Chin", 0, CHIN_OUT, .05);
        chinProp.add("Inner Chin", 0, CHIN_IN, .05);
        chinProp.add("Side Chin", 0, CHIN_SIDE);

        chinLipDist = new DiffBinder(lowerLip.getZProp(ZERO_PROP), chin.getZProp(ZERO_PROP));

        clDist1 = new Binder(chinLipDist.valueProperty().multiply(.33));
        clDist2 = new Binder(chinLipDist.valueProperty().multiply(.67));

        chinH1 = new SumBinder(clDist1.valueProperty(), chin.getZProp(ZERO_PROP));
        chinH2 = new SumBinder(clDist2.valueProperty(), chin.getZProp(ZERO_PROP));

        chinD1 = new SumBinder(lowerLip.getRProp(ZERO_PROP), chinProp.valueProperty("Outer Chin"));
        chinD2 = new DiffBinder(lowerLip.getRProp(ZERO_PROP), chinProp.valueProperty("Inner Chin"));

        chinFront = new ModelBezier(7);

        chinFront.addPoint(new CylCoord(chin.getRProp(ZERO_PROP),
                                        ZERO_PROP,
                                        chin.getZProp(ZERO_PROP)));
        chinFront.addPoint(new CylCoord(chinD1.valueProperty(),
                                        ZERO_PROP,
                                        chinH1.valueProperty()));
        chinFront.addPoint(new CylCoord(chinD2.valueProperty(),
                                        ZERO_PROP,
                                        chinH2.valueProperty()));
        chinFront.addPoint(new CylCoord(lowerLip.getRProp(ZERO_PROP),
                                        ZERO_PROP,
                                        lowerLip.getZProp(ZERO_PROP)));

        mp.add(chinFront);
        chinFront.setPropGroup(chinProp);

        halfMouthWidth = new Binder(mouthProp.valueProperty("Mouth Width").multiply(.5));
        mouthCornerAngle = new ConvertBinder(halfMouthWidth.valueProperty(),
                                             mouthProp.valueProperty("Mouth Depth"));

        clDistSide = new DiffBinder(mouthProp.valueProperty("Mouth Height"),
                                    chin.getZProp(mouthCornerAngle.getTheta()));

        clDist3 = new Binder(chinLipDist.valueProperty().multiply(.5));

        chinH3 = new SumBinder(clDist3.valueProperty(), chin.getZProp(mouthCornerAngle.getTheta()));

        chinD3 = new SumBinder(mouthCornerAngle.getR(),
                               chinProp.valueProperty("Side Chin"));

        chinSide = new ModelBezier[2];

        for (int i = 0; i < 2; ++i)
        {
            chinSide[i] = new ModelBezier(7);

            chinSide[i].addPoint(new CylCoord(chin.getRProp(mouthCornerAngle.getTheta()),
                                              mouthCornerAngle.getTheta(),
                                              chin.getZProp(mouthCornerAngle.getTheta())));
            chinSide[i].addPoint(new CylCoord(chinD3.valueProperty(),
                                              mouthCornerAngle.getTheta(),
                                              chinH3.valueProperty()));
            chinSide[i].addPoint(new CylCoord(mouthCornerAngle.getR(),
                                              mouthCornerAngle.getTheta(),
                                              mouthProp.valueProperty("Mouth Height")));

            if (i < 1)
                chinSide[i].flip();

            mp.add(chinSide[i]);
            chinSide[i].setPropGroup(chinProp);
        }
    }

    private void initNose(ModelPane mp)
    {
        noseProp = new PropGroup("Nose");

        noseProp.add("Nose Width", NOSE_W - .1, NOSE_W + .1);
        noseProp.add("Nose Height", NOSE_Y - .1, NOSE_Y + .1);
        noseProp.add("Nose Depth", NOSE_Z - .05, NOSE_Z + .05);
        noseProp.add("Nose Length", NOSE_L - .05, NOSE_L + .05);
        noseProp.add("Nose Squish", -.7 + Math.PI/2, .3 + Math.PI/2);

        noseProp.add("Septum Width", .02, .07);
        noseProp.add("Nostril Spacing", .01, .05);
        noseProp.add("Nose Zeuth", .02, .1);
        noseProp.add("Nose Forward", 0, .05);
        noseProp.add("Nose Lines Curve", 0, .2);

        noseProp.add("Bridge Width", .02, .05);
        noseProp.add("Bridge Depth", EYES_Z, EYES_Z + .2);

        nose = new ModelParab(7,
                              ZERO_PROP,
                              noseProp.valueProperty("Nose Height"),
                              noseProp.valueProperty("Nose Depth"),
                              noseProp.valueProperty("Nose Width"),
                              noseProp.valueProperty("Nose Length"),
                              noseProp.valueProperty("Nose Squish"));
        mp.add(nose);
        nose.setPropGroup(noseProp);

        noseLowest = new DiffBinder(noseProp.valueProperty("Nose Height"), noseProp.valueProperty("Nose Zeuth"));
        noseLowestFront = new SumBinder(noseProp.valueProperty("Nose Depth"), noseProp.valueProperty("Nose Forward"));

        noseBottom = new ModelBezier[2];
        for (int i = 0; i < 2; ++i)
        {
            noseBottom[i] = new ModelBezier(5);

            noseBottom[i].addPoint(new CylCoord(nose.getRProp(noseProp.valueProperty("Septum Width")),
                                                noseProp.valueProperty("Septum Width"),
                                                nose.getZProp(noseProp.valueProperty("Septum Width"))));

            noseBottom[i].addPoint(new Coord(noseProp.valueProperty("Nostril Spacing"),
                                             noseLowest.valueProperty(),
                                             noseLowestFront.valueProperty()));

            if (i < 1)
                noseBottom[i].flip();
            mp.add(noseBottom[i]);
            noseBottom[i].setPropGroup(noseProp);
        }

        halfNoseWidth = new Binder(noseProp.valueProperty("Nose Width").multiply(.5));
        noseLineCurve = new SumBinder(mouthProp.valueProperty("Mouth Height"), noseProp.valueProperty("Nose Lines Curve"));

        noseLines = new ModelBezier[2];
        for (int i = 0; i < 2; ++i)
        {
            noseLines[i] = new ModelBezier(7, false);

            noseLines[i].addPoint(new Coord(halfNoseWidth.valueProperty(),
                                            noseProp.valueProperty("Nose Height"),
                                            noseProp.valueProperty("Nose Depth")));

            noseLines[i].addPoint(new Coord(halfMouthWidth.valueProperty(),
                                            noseLineCurve.valueProperty(),
                                            mouthProp.valueProperty("Mouth Depth")));

            noseLines[i].addPoint(new Coord(halfMouthWidth.valueProperty(),
                                            mouthProp.valueProperty("Mouth Height"),
                                            mouthProp.valueProperty("Mouth Depth")));

            if (i < 1)
                noseLines[i].flip();
            mp.add(noseLines[i]);
        }

        septum = new ModelBezier[2];
        for (int i = 0; i < 2; ++i)
        {
            septum[i] = new ModelBezier(12);

            septum[i].addPoint(new CylCoord(nose.getRProp(noseProp.valueProperty("Septum Width")),
                                            noseProp.valueProperty("Septum Width"),
                                            nose.getZProp(noseProp.valueProperty("Septum Width"))));

            septum[i].addPoint(new Coord(noseProp.valueProperty("Bridge Width"),
                                         ZERO_PROP,
                                         noseProp.valueProperty("Bridge Depth")));

            if (i < 1)
                septum[i].flip();
            mp.add(septum[i]);
            septum[i].setPropGroup(noseProp);
        }
    }
}
