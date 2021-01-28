package sample;

import java.util.*;

public class Nodes {
    public static DraggableNode nodeDragged = null;
    public static List<DraggableNode> pool = new ArrayList<>();
    public static Integer globalId = 1;
    public static Integer curvesNum = 0;
    public static BlueprintMode prefMode = BlueprintMode.SetImage;
    public static String currentImagePath;
}