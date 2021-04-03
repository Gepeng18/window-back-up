import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Sync {
    private static void sout(int time){
        for(int i = 0;i<time;i++)
            System.out.println();
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        HashMap<String, String> syncPaths = FileOps.getNeedSync();


        // 获取之前文件的时间
        HashMap<String, Long> pathAndTime = FileOps.getMap();

        if (pathAndTime == null) {

            sout(2);System.out.println("-------------- 第一次备份 ---------------");sout(1);
            int backUpfilesNum = 0;
            int filesNum = 0;
            for (Map.Entry<String, String> syncPath : syncPaths.entrySet()) {

                // 获取所有的文件的相对路径
                ArrayList<String> relativePaths = FileOps.getRelativePaths(syncPath.getValue());
                filesNum+=relativePaths.size();
                for (String relativePath : relativePaths) {
                    FileOps.syncFile(syncPath.getKey(), syncPath.getValue(), relativePath);
                    System.out.println(syncPath.getValue() + "\\" + relativePath + "   已备份");
                    backUpfilesNum++;
                }
            }
            sout(1);
            System.out.println("一共有 "+filesNum+" 个文件，成功备份 "+backUpfilesNum+" 个文件");
            if(filesNum!=backUpfilesNum){
                for (int i = 0; i < 100; i++) {
                    System.out.print("错误");
                }
            }

        } else {
            int backUpfilesNum = 0;
            int filesNum = 0;

            sout(2);System.out.println("-------------- 备份开始 ---------------");sout(1);
            for (Map.Entry<String, String> syncPath : syncPaths.entrySet()) {
                ArrayList<String> relativePaths = FileOps.judgeMod(syncPath.getKey(), pathAndTime, syncPath.getValue());
                filesNum+=relativePaths.size();
                for (String relativePath : relativePaths) {
                    FileOps.syncFile(syncPath.getKey(), syncPath.getValue(), relativePath);
                    System.out.println(syncPath.getValue() + "\\" + relativePath + "   已备份");
                    backUpfilesNum++;
                }


                ArrayList<String> remainPaths = FileOps.getRelativePaths(syncPath.getValue());

                for (String relativePath : remainPaths) {

                    String dirPath = "";
                    dirPath = FileOps.getSyncPath() + "\\" + syncPath.getKey() + "\\" + relativePath;
                    if (!new File(dirPath).exists()) {
                        FileOps.syncFile(syncPath.getKey(), syncPath.getValue(), relativePath);
                        System.out.println(syncPath.getValue() + "\\" + relativePath + "   已备份");
                        backUpfilesNum++;
                        filesNum ++;
                    }
                }
            }

            sout(1);
            System.out.println("一共有 "+filesNum+" 个文件，成功备份 "+backUpfilesNum+" 个文件");
            sout(1);

            if(filesNum!=backUpfilesNum){
                for (int i = 0; i < 100; i++) {
                    System.out.print("错误");
                }
            }
        }


        // 存储新的更新时间
        HashMap<String, Long> newPathAndTime = new HashMap<>();
        for (Map.Entry<String, String> entry : syncPaths.entrySet()) {

            newPathAndTime.putAll(FileOps.initModTime(entry.getKey(), entry.getValue()));
        }
        FileOps.mapToFile(newPathAndTime, FileOps.getMapPath());

        sout(1);
        System.out.println("-------------- 备份结束 ---------------");
        sout(2);
    }

}
