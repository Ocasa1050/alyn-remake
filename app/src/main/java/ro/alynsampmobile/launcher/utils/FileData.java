package ro.alynsampmobile.launcher.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import com.joom.paranoid.Obfuscate;

@Obfuscate
public class FileData {
    private final String name;
    private final String path;
    private final long size;
    private final long hash;
    private final String url;
    private final String gpu;

    public FileData(String name, String path, long size, long hash, String url, String gpu) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.hash = hash;
        this.url = url;
        this.gpu = normalizeGpu(gpu);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }

    public String getGpu() {
        return gpu;
    }

    private static String normalizeGpu(String gpu) {
        if (gpu == null) {
            return "all";
        }

        String normalized = gpu.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "all" : normalized;
    }

    public static ArrayList<FileData> getListByJson(JSONObject json) throws JSONException {
        ArrayList<FileData> list = new ArrayList<>();
        if (!json.has("files")) return list;
        JSONArray arr = json.getJSONArray("files");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String path = obj.optString("path", "");
            String name = obj.optString("name", "");
            if (name.isEmpty()) {
                int separator = path.lastIndexOf('/');
                name = separator >= 0 ? path.substring(separator + 1) : path;
            }
            long size = 0;
            try {
                size = obj.optLong("size", 0);
                if (size == 0 && obj.has("size")) {
                    size = Long.parseLong(obj.getString("size"));
                }
            } catch (Exception ignored) {
            }
            long hash = obj.optLong("hash", 0);
            String url = obj.optString("url", "");
            String gpu = obj.optString("gpu", "all");
            list.add(new FileData(name, path, size, hash, url, gpu));
        }
        return list;
    }

    public boolean isZipArchive() {
        return url.toLowerCase(Locale.ROOT).endsWith(".zip");
    }
}
