import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FileOps {

    private static final String settingPath = ".\\setting.txt";
    private static final String mapPath = ".\\.important";

    public static String getSettingPath() {
        return settingPath;
    }

    public static String getMapPath() {
        return mapPath;
    }

    private static void copyOrCutFile(String oriPath, String dstPath, boolean delete) throws IOException {
        InputStream in = new FileInputStream(oriPath);
        OutputStream out = new FileOutputStream(dstPath);
        //操作 (分段读取)
        byte[] flush = new byte[1024]; //缓冲容器
        int len = -1; //接收长度
        while ((len = in.read(flush)) != -1) {
            out.write(flush, 0, len); //分段写出
        }
        out.flush();

        if (delete)
            new File(oriPath).delete();
    }

    public static void copyFile(String oriPath, String dstPath) throws IOException {
        copyOrCutFile(oriPath, dstPath, false);
    }

    public static void cutFile(String oriPath, String dstPath) throws IOException{
        copyOrCutFile(oriPath, dstPath, true);
    }

    private static ArrayList<String> getFilePathInDir(String path) {
        ArrayList<String> res = new ArrayList<>();
        getFilePathInDir(path, res);
        return res;
    }

    // 转换成相对路径
    public static String getRelativePath(String base, String path) {
        int length = base.length();
        assert path.substring(0, length).equals(base);
        if (path.equals(base))
            return "";
        return path.substring(length + 1);

    }

    // 打开该Path，将里面的文件加入filesPath
    private static void getFilePathInDir(String path, ArrayList<String> filesPath) {
        File curFile = new File(path);
        File[] files = curFile.listFiles();
        if (files == null || files.length == 0)
            return;
        for (File file : files) {
            if (file.isDirectory()) {
                getFilePathInDir(file.getAbsolutePath(), filesPath);
            } else
                filesPath.add(file.getAbsolutePath());
        }
    }


    public static ArrayList<String> getRelativePaths(String path) {
        ArrayList<String> relativePaths = new ArrayList<>();
        ArrayList<String> absolutePaths = FileOps.getFilePathInDir(path);
        for (String absolutePath : absolutePaths) {
            String relativePath = FileOps.getRelativePath(path, absolutePath);
            relativePaths.add(relativePath);
        }
        return relativePaths;
    }

    private static long getModTime(String path) {
        return new File(path).lastModified();
    }


    //把ArrayList集合的数据存储到文本文件中
    public static void mapToFile(HashMap<String, Long> files, String dstPath) throws IOException {
        new File(dstPath).delete();
        ObjectOutputStream oos = null;

        oos = new ObjectOutputStream(new FileOutputStream(dstPath));
        oos.writeObject(files);                //从容器中取数据并输出到文件中
        oos.flush();
        oos.close();

    }

    //把ArrayList集合的数据存储到文本文件中
    public static HashMap<String, Long> fileToMap(String Path) throws IOException, ClassNotFoundException {
        HashMap<String, Long> res = null;
        ObjectInputStream ois = null;

        ois = new ObjectInputStream(new FileInputStream(Path));
        res = (HashMap<String, Long>) ois.readObject();        //从文件中读出数据
        ois.close();
        return res;

    }

    public static HashMap<String, Long> initModTime(String group, String dirAbolutePath) {
        HashMap<String, Long> pathAndTime = new HashMap<String, Long>();
        ArrayList<String> fileAbsolutePaths = getFilePathInDir(dirAbolutePath);
        for (String fileAbsolutePath : fileAbsolutePaths) {
            long l = new File(fileAbsolutePath).lastModified();
            String relativePath = getRelativePath(dirAbolutePath, fileAbsolutePath);
            pathAndTime.put(group + "#" + relativePath, l);
        }
        return pathAndTime;
    }

    public static ArrayList<String> judgeMod(String group, HashMap<String, Long> pathAndTime, String dirAbsolutePath) {
        ArrayList<String> modFiles = new ArrayList<>();


        ArrayList<String> filesAbsolutePaths = getFilePathInDir(dirAbsolutePath);

        for (String filesAbsolutePath : filesAbsolutePaths) {
            String relativeFilePath = getRelativePath(dirAbsolutePath, filesAbsolutePath);
            Long oldTime = pathAndTime.get(group + "#" + relativeFilePath);
            // 文件是新创建的
            if (oldTime == null) {
                modFiles.add(relativeFilePath);
                continue;
            }
            // 文件没有修改
            long modTime = new File(filesAbsolutePath).lastModified();
            if (modTime - oldTime < 1000)
                continue;

            // 文件修改过
            modFiles.add(relativeFilePath);

        }
        return modFiles;
    }

    public static HashMap<String, Long> getMap() throws IOException, ClassNotFoundException {
        if (!new File(mapPath).exists())
            return null;
        return fileToMap(mapPath);
    }

    private static String getNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private static HashMap<String, String> readSetting() throws IOException {
        String path = getSettingPath();
        if (!new File(path).exists())
            throw new RuntimeException("配置文件不存在");
        HashMap<String, String> setting = new HashMap<>();
        BufferedReader in;

        in = new BufferedReader(new FileReader(path));
        int lineIndex = 0;
        String line;
        while ((line = in.readLine()) != null) {
//                System.out.println(line);
            if (line.startsWith("//") || line.startsWith("#") || line.equals("") || line.startsWith("     "))
                continue;
            if (lineIndex == 0) {
                if (!(line.split("#")[0].trim().equals("sync_path")))
                    throw new RuntimeException("第一行必须以sync_path -- 开头");
                setting.put("sync_path", line.split("#")[1].trim());
            } else {
                if (!(line.split("#").length == 2))
                    throw new RuntimeException("备份文件夹必须以:分割成两段，如 论文 -- E:\\gepeng");
                setting.put(line.split("#")[0].trim(), line.split("#")[1].trim());
            }
            lineIndex++;

        }
        in.close();
        return setting;


    }

    public static HashMap<String, String> getNeedSync() throws IOException {
        HashMap<String, String> setting = new HashMap<>(readSetting());
        setting.remove("sync_path");
        return setting;
    }

    public static String getSyncPath() throws IOException {
        return readSetting().get("sync_path");

    }

    public static void syncFile(String group, String dirAbsolutePath, String fileRelativePath) throws IOException {

        String fileAbsolutePath = dirAbsolutePath + "\\" + fileRelativePath;

        String fileName = new File(fileAbsolutePath).getName();

        String syncDir = "";
        if (fileRelativePath.contains("\\"))
            syncDir = getSyncPath() + "\\" + group + "\\" + fileRelativePath.substring(0, fileRelativePath.lastIndexOf("\\")) + "\\" + fileName;
        else
            syncDir = getSyncPath() + "\\" + group + "\\" + fileName;

        // 防止有文件名是空格，会报dst_file找不到
        File syncDirFile = new File(syncDir.trim());
        if (!syncDirFile.exists())
            syncDirFile.mkdirs();

        String name = "";
        String fileExtName = "";
        String newFilePath = "";
        int index = fileName.lastIndexOf(".");
        if (index!=-1){
            name = fileName.substring(0,index);
            fileExtName = fileName.substring(index + 1);
            newFilePath = syncDir.trim() + "\\" + name + "-" + getNow() + "." + fileExtName;
        }else{
            name = fileName;
            fileExtName = "";
            newFilePath = syncDir.trim() + "\\" + name + "-" + getNow();
        }


        copyFile(fileAbsolutePath, newFilePath);
    }


}
