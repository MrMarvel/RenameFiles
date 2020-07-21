import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final File dir = new File("D:/WpSystem/S-1-5-21-3602815801-4121317279-684615630-1004/AppData/Local/Packages/53309GeekIT.AniLibria_9vh2d0xbyfdde/TempState");
    private static final File hmsDir = new File("D:/WpSystem/S-1-5-21-3602815801-4121317279-684615630-1004/AppData/Local/Packages/53309GeekIT.AniLibria_9vh2d0xbyfdde/TempState/forHMS");
    private static final HashMap<File, File> filesToCopy = new HashMap<>();//to;from

    public static void main(String[] args) {
        if (!hmsDir.exists()) {
            if(hmsDir.mkdir()) System.out.println("Каталог "+hmsDir.getName()+" успешно создан!");
        }


        Scanner sc = new Scanner(System.in);

        if (Objects.requireNonNull(hmsDir.listFiles()).length > 0) {
            System.out.println(String.format("Найдены файлы в %s. Предпочитаете удалить? Y/N",hmsDir.getName()));
            String msg = sc.next();
            if (msg.toUpperCase().equals("Y") || msg.equals("1")) {
                Arrays.stream(Objects.requireNonNull(hmsDir.listFiles())).forEach(f -> {
                    System.out.print(String.format("Trying to delete %s. ",f.getName()));
                    if (!f.delete()) System.out.println("ERROR DELETING"); else System.out.println("SUCCESSFUL");
                });
            }
        }
	    List<File> videos = getFiles(dir);
	    int start = 43;
	    int end = 42;
	    AtomicInteger number = new AtomicInteger(0);
	    videos.forEach(f -> {
	        String name = f.getName();
	        int index = name.indexOf("(");
	        int lastIndex = name.lastIndexOf(")");
	        int dtIndex = lastIndex-index-1;
	        if (lastIndex < 0 || index < 0) return;
	        String num = name.substring(index+1,lastIndex);
	        Integer x = null;
	        try {
	            x = Integer.parseInt(num);
            } catch (NumberFormatException ignored) {}
            if (x != null && x >= start && (x <= end || end < start) && !name.contains(".")) {
                number.getAndIncrement();
                String newName = hmsDir.getAbsolutePath()+"/"+name.replace(x.toString(),number+"")+".mp4";
                File newFile = new File(newName);
	            System.out.println(name+" will have been copying to "+hmsDir.getName()+"/"+newFile.getName()+"!");
	            filesToCopy.put(newFile, f);

                //if (f.renameTo(new File(newName))) System.out.println("COMPLETED"); else System.out.println("ERROR");
            }
        });
        System.out.println("SEARCH COMPLETE");
        System.out.println("Скопировать эти файлы? Y/N");
        String msg = sc.next();

	    if (filesToCopy.size() > 0) {
            if (msg.toUpperCase().equals("Y") || msg.equals("1")) {
                System.out.println("Trying to copy.");
                filesToCopy.forEach((k,v)->{
                    TaskCopyFiles task = new TaskCopyFiles();
                    task.execute(new File[]{k, v});
                });
            }
        }
    }

    static List<File> getDirs(File parent, int level){
        List<File> dirs = new ArrayList<>(); //to store
        for(File f: Objects.requireNonNull(parent.listFiles())){
            if(f.isDirectory()) {
                if (level==0) dirs.add(f);
                else
                if (level > 0) dirs.addAll(getDirs(f,level-1)); //recursion
            }
        }
        return dirs;
    }

    static List<File> getFiles(final File parent){
        List<File> files = new ArrayList<>(); //to store
        for(File f: Objects.requireNonNull(parent.listFiles())){
            if(f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }
}

abstract class AsyncTask<Params, Progress, Result> extends Thread {
    private boolean runned = false;
    private Params[] params;
    private Result result;

    protected void onPreExecute(Params... params) {

    }
    protected void onPostExecute(Result result) {

    }
    protected void onProgressUpdate(Progress... progress) {

    }
    abstract protected Result doInBackground(Params... params);
    protected final Result execute(Params... params) {
        this.params = params;
        if (!runned) {
            runned = true;
            start();
            return result;
        }
        return null;
    }

    @Override
    public void run() {
        onPreExecute(params);
        result = doInBackground(params);
        onPostExecute(result);
    }
}

class TaskCopyFiles extends AsyncTask<File[], Void, Integer> {
    private static int pended = 0;
    private static int tried = 0;
    private static int errors = 0;
    private File copyFrom;
    private File copyTo;
    
    @Override
    protected Integer doInBackground(File[]... files) {
        if (files.length < 1) return -1;
        File[] filesToCopy = files[0];
        if (filesToCopy == null) return -2;
        if (filesToCopy.length < 2) return -3;
        copyFrom = filesToCopy[1];
        copyTo = filesToCopy[0];
        try {
            Files.copy(copyFrom.toPath(),copyTo.toPath());
        } catch (IOException e) {
            return -4;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        tried++;
        if (result < 0) {
            errors++;
            System.out.println(String.format("Error copying %s to %s",copyFrom.getName(),copyTo.getAbsolutePath()));
        } else {
            System.out.println(String.format("Successfully copied %s!",copyTo.getName()));
        }
        if (tried >= pended) {
            System.out.println(String.format("%d/%d OK  %d/%d ERRORRED",pended-errors, pended, errors, pended));
        }
    }

    @Override
    public void run() {
        pended++;
        super.run();
    }
}
