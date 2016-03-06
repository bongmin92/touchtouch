/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;

import org.telegram.android.video.InputSurface;
import org.telegram.android.video.MP4Builder;
import org.telegram.android.video.Mp4Movie;
import org.telegram.android.video.OutputSurface;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Cells.ChatMediaCell;
import org.telegram.ui.Components.GifDrawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class MediaController implements NotificationCenter.NotificationCenterDelegate {

    private native int startRecord(String path);
    private native int writeFrame(ByteBuffer frame, int len);
    private native void stopRecord();
    private native int openOpusFile(String path);
    private native int seekOpusFile(float position);
    private native int isOpusFile(String path);
    private native void closeOpusFile();
    private native void readOpusFile(ByteBuffer buffer, int capacity, int[] args);
    private native long getTotalPcmDuration();

    public static int[] readArgs = new int[3];

    public static interface FileDownloadProgressListener {
        public void onFailedDownload(String fileName);
        public void onSuccessDownload(String fileName);
        public void onProgressDownload(String fileName, float progress);
        public void onProgressUpload(String fileName, float progress, boolean isEncrypted);
        public int getObserverTag();
    }

    private class AudioBuffer {
        public AudioBuffer(int capacity) {
            buffer = ByteBuffer.allocateDirect(capacity);
            bufferBytes = new byte[capacity];
        }

        ByteBuffer buffer;
        byte[] bufferBytes;
        int size;
        int finished;
        long pcmOffset;
    }

    private static final String[] projectionPhotos = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION
    };

    public static class AlbumEntry {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public ArrayList<PhotoEntry> photos = new ArrayList<PhotoEntry>();

        public AlbumEntry(int bucketId, String bucketName, PhotoEntry coverPhoto) {
            this.bucketId = bucketId;
            this.bucketName = bucketName;
            this.coverPhoto = coverPhoto;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            photos.add(photoEntry);
        }
    }

    public static class PhotoEntry {
        public int bucketId;
        public int imageId;
        public long dateTaken;
        public String path;
        public int orientation;

        public PhotoEntry(int bucketId, int imageId, long dateTaken, String path, int orientation) {
            this.bucketId = bucketId;
            this.imageId = imageId;
            this.dateTaken = dateTaken;
            this.path = path;
            this.orientation = orientation;
        }
    }

    public final static String MIME_TYPE = "video/avc";
    private final static int PROCESSOR_TYPE_OTHER = 0;
    private final static int PROCESSOR_TYPE_QCOM = 1;
    private final static int PROCESSOR_TYPE_INTEL = 2;
    private final static int PROCESSOR_TYPE_MTK = 3;
    private final static int PROCESSOR_TYPE_SEC = 4;
    private final static int PROCESSOR_TYPE_TI = 5;
    private final Object videoConvertSync = new Object();

    private ArrayList<MessageObject> videoConvertQueue = new ArrayList<MessageObject>();
    private final Object videoQueueSync = new Object();
    private boolean cancelCurrentVideoConversion = false;
    private boolean videoConvertFirstWrite = true;

    public static final int AUTODOWNLOAD_MASK_PHOTO = 1;
    public static final int AUTODOWNLOAD_MASK_AUDIO = 2;
    public static final int AUTODOWNLOAD_MASK_VIDEO = 4;
    public static final int AUTODOWNLOAD_MASK_DOCUMENT = 8;
    public int mobileDataDownloadMask = 0;
    public int wifiDownloadMask = 0;
    public int roamingDownloadMask = 0;
    private int lastCheckMask = 0;
    private ArrayList<DownloadObject> photoDownloadQueue = new ArrayList<DownloadObject>();
    private ArrayList<DownloadObject> audioDownloadQueue = new ArrayList<DownloadObject>();
    private ArrayList<DownloadObject> documentDownloadQueue = new ArrayList<DownloadObject>();
    private ArrayList<DownloadObject> videoDownloadQueue = new ArrayList<DownloadObject>();
    private HashMap<String, DownloadObject> downloadQueueKeys = new HashMap<String, DownloadObject>    ();

    private boolean saveToGallery = true;

    private HashMap<String, ArrayList<WeakReference<FileDownloadProgressListener>>> loadingFileObservers = new HashMap<String, ArrayList<WeakReference<FileDownloadProgressListener>>>();
    private HashMap<Integer, String> observersByTag = new HashMap<Integer, String>();
    private boolean listenerInProgress = false;
    private HashMap<String, FileDownloadProgressListener> addLaterArray = new HashMap<String, FileDownloadProgressListener>();
    private ArrayList<FileDownloadProgressListener> deleteLaterArray = new ArrayList<FileDownloadProgressListener>();
    private int lastTag = 0;

    private GifDrawable currentGifDrawable;
    private MessageObject currentGifMessageObject;
    private ChatMediaCell currentMediaCell;

    private boolean isPaused = false;
    private MediaPlayer audioPlayer = null;
    private AudioTrack audioTrackPlayer = null;
    private int lastProgress = 0;
    private MessageObject playingMessageObject;
    private int playerBufferSize = 0;
    private boolean decodingFinished = false;
    private long currentTotalPcmDuration;
    private long lastPlayPcm;
    private int ignoreFirstProgress = 0;
    private Timer progressTimer = null;
    private final Object progressTimerSync = new Object();

    private AudioRecord audioRecorder = null;
    private TLRPC.TL_audio recordingAudio = null;
    private File recordingAudioFile = null;
    private long recordStartTime;
    private long recordTimeCount;
    private long recordDialogId;
    private DispatchQueue fileDecodingQueue;
    private DispatchQueue playerQueue;
    private ArrayList<AudioBuffer> usedPlayerBuffers = new ArrayList<AudioBuffer>();
    private ArrayList<AudioBuffer> freePlayerBuffers = new ArrayList<AudioBuffer>();
    private final Object playerSync = new Object();
    private final Object playerObjectSync = new Object();

    private final Object sync = new Object();

    private ArrayList<ByteBuffer> recordBuffers = new ArrayList<ByteBuffer>();
    private ByteBuffer fileBuffer;
    private int recordBufferSize;
    private boolean sendAfterDone;

    private DispatchQueue recordQueue;
    private DispatchQueue fileEncodingQueue;

    private class InternalObserver extends ContentObserver {
        public InternalObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

        }
    }

    private class ExternalObserver extends ContentObserver {
        public ExternalObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

        }
    }

    private ExternalObserver externalObserver = null;
    private InternalObserver internalObserver = null;
    private long lastSecretChatEnterTime = 0;
    private long lastSecretChatLeaveTime = 0;
    private long lastMediaCheckTime = 0;
    private TLRPC.EncryptedChat lastSecretChat = null;
    private ArrayList<Long> lastSecretChatVisibleMessages = null;
    private int startObserverToken = 0;
    private StopMediaObserverRunnable stopMediaObserverRunnable = null;
    private final class StopMediaObserverRunnable implements Runnable {
        public int currentObserverToken = 0;

        @Override
        public void run() {
            if (currentObserverToken == startObserverToken) {
                try {
                    if (internalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(internalObserver);
                        internalObserver = null;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                try {
                    if (externalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(externalObserver);
                        externalObserver = null;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        }
    }
    private String[] mediaProjections = null;

    private static volatile MediaController Instance = null;
    public static MediaController getInstance() {
        MediaController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MediaController();
                }
            }
        }
        return localInstance;
    }

    public MediaController() {
        try {

        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        fileBuffer = ByteBuffer.allocateDirect(1920);
        recordQueue = new DispatchQueue("recordQueue");
        recordQueue.setPriority(Thread.MAX_PRIORITY);
        fileEncodingQueue = new DispatchQueue("fileEncodingQueue");
        fileEncodingQueue.setPriority(Thread.MAX_PRIORITY);
        playerQueue = new DispatchQueue("playerQueue");
        fileDecodingQueue = new DispatchQueue("fileDecodingQueue");

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        mobileDataDownloadMask = preferences.getInt("mobileDataDownloadMask", AUTODOWNLOAD_MASK_PHOTO | AUTODOWNLOAD_MASK_AUDIO);
        wifiDownloadMask = preferences.getInt("wifiDownloadMask", AUTODOWNLOAD_MASK_PHOTO | AUTODOWNLOAD_MASK_AUDIO);
        roamingDownloadMask = preferences.getInt("roamingDownloadMask", 0);
        saveToGallery = preferences.getBoolean("save_gallery", false);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileUploadProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDeleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.removeAllMessagesFromDialog);

        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ApplicationLoader.applicationContext.registerReceiver(networkStateReceiver, filter);

        if (UserConfig.isClientActivated()) {

        }

        if (Build.VERSION.SDK_INT >= 16) {
            mediaProjections = new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.TITLE,
                    MediaStore.Images.ImageColumns.WIDTH,
                    MediaStore.Images.ImageColumns.HEIGHT
            };
        } else {
            mediaProjections = new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.TITLE
            };
        }
    }

    public void setLastEncryptedChatParams(long enterTime, long leaveTime, TLRPC.EncryptedChat encryptedChat, ArrayList<Long> visibleMessages) {
        lastSecretChatEnterTime = enterTime;
        lastSecretChatLeaveTime = leaveTime;
        lastSecretChat = encryptedChat;
        lastSecretChatVisibleMessages = visibleMessages;
    }

    public int generateObserverTag() {
        return lastTag++;
    }

    public void addLoadingFileObserver(String fileName, FileDownloadProgressListener observer) {
        if (listenerInProgress) {
            addLaterArray.put(fileName, observer);
            return;
        }
        removeLoadingFileObserver(observer);

        ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
        if (arrayList == null) {
            arrayList = new ArrayList<WeakReference<FileDownloadProgressListener>>();
            loadingFileObservers.put(fileName, arrayList);
        }
        arrayList.add(new WeakReference<FileDownloadProgressListener>(observer));

        observersByTag.put(observer.getObserverTag(), fileName);
    }

    public void removeLoadingFileObserver(FileDownloadProgressListener observer) {
        if (listenerInProgress) {
            deleteLaterArray.add(observer);
            return;
        }
        String fileName = observersByTag.get(observer.getObserverTag());
        if (fileName != null) {
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                for (int a = 0; a < arrayList.size(); a++) {
                    WeakReference<FileDownloadProgressListener> reference = arrayList.get(a);
                    if (reference.get() == null || reference.get() == observer) {
                        arrayList.remove(a);
                        a--;
                    }
                }
                if (arrayList.isEmpty()) {
                    loadingFileObservers.remove(fileName);
                }
            }
            observersByTag.remove(observer.getObserverTag());
        }
    }

    private void processLaterArrays() {
        for (HashMap.Entry<String, FileDownloadProgressListener> listener : addLaterArray.entrySet()) {
            addLoadingFileObserver(listener.getKey(), listener.getValue());
        }
        addLaterArray.clear();
        for (FileDownloadProgressListener listener : deleteLaterArray) {
            removeLoadingFileObserver(listener);
        }
        deleteLaterArray.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.FileDidFailedLoad) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onFailedDownload(fileName);
                        observersByTag.remove(reference.get().getObserverTag());
                    }
                }
                loadingFileObservers.remove(fileName);
            }
            listenerInProgress = false;
            processLaterArrays();

        } else if (id == NotificationCenter.FileDidLoaded) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onSuccessDownload(fileName);
                        observersByTag.remove(reference.get().getObserverTag());
                    }
                }
                loadingFileObservers.remove(fileName);
            }
            listenerInProgress = false;
            processLaterArrays();

        } else if (id == NotificationCenter.FileLoadProgressChanged) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                Float progress = (Float)args[1];
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onProgressDownload(fileName, progress);
                    }
                }
            }
            listenerInProgress = false;
            processLaterArrays();
        } else if (id == NotificationCenter.FileUploadProgressChanged) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                Float progress = (Float)args[1];
                Boolean enc = (Boolean)args[2];
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onProgressUpload(fileName, progress, enc);
                    }
                }
            }
            listenerInProgress = false;
            processLaterArrays();
        } else if (id == NotificationCenter.messagesDeleted) {
            if (playingMessageObject != null) {
                ArrayList<Integer> markAsDeletedMessages = (ArrayList<Integer>)args[0];

            }
        } else if (id == NotificationCenter.removeAllMessagesFromDialog) {
            long did = (Long)args[0];

        }
    }









    public static void saveFile(String fullPath, Context context, final int type, final String name) {
        if (fullPath == null) {
            return;
        }

        File file = null;
        if (fullPath != null && fullPath.length() != 0) {
            file = new File(fullPath);
            if (!file.exists()) {
                file = null;
            }
        }

        if (file == null) {
            return;
        }

        final File sourceFile = file;
        if (sourceFile.exists()) {
            ProgressDialog progressDialog = null;
            if (context != null) {
                try {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMax(100);
                    progressDialog.show();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }

            final ProgressDialog finalProgress = progressDialog;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File destFile = null;
                        if (type == 0) {
                            destFile = Utilities.generatePicturePath();
                        } else if (type == 1) {
                            destFile = Utilities.generateVideoPath();
                        } else if (type == 2) {
                            File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            destFile = new File(f, name);
                        }

                        if(!destFile.exists()) {
                            destFile.createNewFile();
                        }
                        FileChannel source = null;
                        FileChannel destination = null;
                        boolean result = true;
                        long lastProgress = System.currentTimeMillis() - 500;
                        try {
                            source = new FileInputStream(sourceFile).getChannel();
                            destination = new FileOutputStream(destFile).getChannel();
                            long size = source.size();
                            for (long a = 0; a < size; a += 1024) {
                                destination.transferFrom(source, a, Math.min(1024, size - a));
                                if (finalProgress != null) {
                                    if (lastProgress <= System.currentTimeMillis() - 500) {
                                        lastProgress = System.currentTimeMillis();
                                        final int progress = (int) ((float) a / (float) size * 100);
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    finalProgress.setProgress(progress);
                                                } catch (Exception e) {
                                                    FileLog.e("tmessages", e);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                            result = false;
                        } finally {
                            if(source != null) {
                                source.close();
                            }
                            if(destination != null) {
                                destination.close();
                            }
                        }

                        if (result && (type == 0 || type == 1)) {
                            Utilities.addMediaToGallery(Uri.fromFile(destFile));
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                    if (finalProgress != null) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    finalProgress.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public GifDrawable getGifDrawable(ChatMediaCell cell, boolean create) {
        if (cell == null) {
            return null;
        }

        MessageObject messageObject = cell.getMessageObject();
        if (messageObject == null) {
            return null;
        }

        if (currentGifDrawable != null && currentGifMessageObject != null && messageObject.messageOwner.id == currentGifMessageObject.messageOwner.id) {
            currentMediaCell = cell;
            currentGifDrawable.parentView = new WeakReference<View>(cell);
            return currentGifDrawable;
        }

        if (create) {
            if (currentMediaCell != null) {
                if (currentGifDrawable != null) {
                    currentGifDrawable.stop();
                    currentGifDrawable.recycle();
                }
                currentMediaCell.clearGifImage();
            }
            currentGifMessageObject = cell.getMessageObject();
            currentMediaCell = cell;

            File cacheFile = null;
            if (currentGifMessageObject.messageOwner.attachPath != null && currentGifMessageObject.messageOwner.attachPath.length() != 0) {
                File f = new File(currentGifMessageObject.messageOwner.attachPath);
                if (f.length() > 0) {
                    cacheFile = f;
                }
            }
            if (cacheFile == null) {
                cacheFile = FileLoader.getPathToMessage(messageObject.messageOwner);
            }
            try {
                currentGifDrawable = new GifDrawable(cacheFile);
                currentGifDrawable.parentView = new WeakReference<View>(cell);
                return currentGifDrawable;
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        return null;
    }

    public void clearGifDrawable(ChatMediaCell cell) {
        if (cell == null) {
            return;
        }

        MessageObject messageObject = cell.getMessageObject();
        if (messageObject == null) {
            return;
        }

        if (currentGifMessageObject != null && messageObject.messageOwner.id == currentGifMessageObject.messageOwner.id) {
            if (currentGifDrawable != null) {
                currentGifDrawable.stop();
                currentGifDrawable.recycle();
                currentGifDrawable = null;
            }
            currentMediaCell = null;
            currentGifMessageObject = null;
        }
    }

    public static boolean isGif(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        try {
            parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
            input = new FileInputStream(parcelFD.getFileDescriptor());
            if (input.getChannel().size() > 3) {
                byte[] header = new byte[3];
                input.read(header, 0, 3);
                String str = new String(header);
                if (str != null && str.equalsIgnoreCase("gif")) {
                    return true;
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        } finally {
            try {
                if (parcelFD != null) {
                    parcelFD.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
        }
        return false;
    }

    public static String copyDocumentToCache(Uri uri, String ext) {
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            int id = UserConfig.lastLocalId;
            UserConfig.lastLocalId--;
            parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
            input = new FileInputStream(parcelFD.getFileDescriptor());
            File f = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), String.format(Locale.US, "%d.%s", id, ext));
            output = new FileOutputStream(f);
            input.getChannel().transferTo(0, input.getChannel().size(), output.getChannel());
            UserConfig.saveConfig(false);
            return f.getAbsolutePath();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        } finally {
            try {
                if (parcelFD != null) {
                    parcelFD.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
        }
        return null;
    }

    public void toggleSaveToGallery() {
        saveToGallery = !saveToGallery;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("save_gallery", saveToGallery);
        editor.commit();
        checkSaveToGalleryFiles();
    }

    public void checkSaveToGalleryFiles() {
        try {
            File telegramPath = new File(Environment.getExternalStorageDirectory(), "Telegram");
            File imagePath = new File(telegramPath, "Telegram Images");
            imagePath.mkdir();
            File videoPath = new File(telegramPath, "Telegram Video");
            videoPath.mkdir();

            if (saveToGallery) {
                if (imagePath.isDirectory()) {
                    new File(imagePath, ".nomedia").delete();
                }
                if (videoPath.isDirectory()) {
                    new File(videoPath, ".nomedia").delete();
                }
            } else {
                if (imagePath.isDirectory()) {
                    new File(imagePath, ".nomedia").createNewFile();
                }
                if (videoPath.isDirectory()) {
                    new File(videoPath, ".nomedia").createNewFile();
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public boolean canSaveToGallery() {
        return saveToGallery;
    }

    public static void loadGalleryPhotosAlbums(final int guid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<AlbumEntry> albumsSorted = new ArrayList<AlbumEntry>();
                HashMap<Integer, AlbumEntry> albums = new HashMap<Integer, AlbumEntry>();
                AlbumEntry allPhotosAlbum = null;
                String cameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";
                Integer cameraAlbumId = null;

                Cursor cursor = null;
                try {
                    cursor = MediaStore.Images.Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                    if (cursor != null) {
                        int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                        int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                        int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                        while (cursor.moveToNext()) {
                            int imageId = cursor.getInt(imageIdColumn);
                            int bucketId = cursor.getInt(bucketIdColumn);
                            String bucketName = cursor.getString(bucketNameColumn);
                            String path = cursor.getString(dataColumn);
                            long dateTaken = cursor.getLong(dateColumn);
                            int orientation = cursor.getInt(orientationColumn);

                            if (path == null || path.length() == 0) {
                                continue;
                            }

                            PhotoEntry photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation);

                            if (allPhotosAlbum == null) {
                                allPhotosAlbum = new AlbumEntry(0, LocaleController.getString("AllPhotos", R.string.AllPhotos), photoEntry);
                                albumsSorted.add(0, allPhotosAlbum);
                            }
                            if (allPhotosAlbum != null) {
                                allPhotosAlbum.addPhoto(photoEntry);
                            }

                            AlbumEntry albumEntry = albums.get(bucketId);
                            if (albumEntry == null) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry);
                                albums.put(bucketId, albumEntry);
                                if (cameraAlbumId == null && cameraFolder != null && path != null && path.startsWith(cameraFolder)) {
                                    albumsSorted.add(0, albumEntry);
                                    cameraAlbumId = bucketId;
                                } else {
                                    albumsSorted.add(albumEntry);
                                }
                            }

                            albumEntry.addPhoto(photoEntry);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                } finally {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
                final Integer cameraAlbumIdFinal = cameraAlbumId;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.albumsDidLoaded, guid, albumsSorted, cameraAlbumIdFinal);
                    }
                });
            }
        }).start();
    }



    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    @TargetApi(16)
    private int selectTrack(MediaExtractor extractor, boolean audio) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (audio) {
                if (mime.startsWith("audio/")) {
                    return i;
                }
            } else {
                if (mime.startsWith("video/")) {
                    return i;
                }
            }
        }
        return -5;
    }

    private void didWriteData(final MessageObject messageObject, final File file, final boolean last, final boolean error) {
        final boolean firstWrite = videoConvertFirstWrite;
        if (firstWrite) {
            videoConvertFirstWrite = false;
        }
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (error) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.FilePreparingFailed, messageObject, file.toString());
                } else {
                    if (firstWrite) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FilePreparingStarted, messageObject, file.toString());
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileNewChunkAvailable, messageObject, file.toString(), last ? file.length() : 0);
                }
                if (error || last) {
                    synchronized (videoConvertSync) {
                        cancelCurrentVideoConversion = false;
                    }
                    videoConvertQueue.remove(messageObject);
                }
            }
        });
    }

    @TargetApi(16)
    private long readAndWriteTrack(final MessageObject messageObject, MediaExtractor extractor, MP4Builder mediaMuxer, MediaCodec.BufferInfo info, long start, long end, File file, boolean isAudio) throws Exception {
        int trackIndex = selectTrack(extractor, isAudio);
        if (trackIndex >= 0) {
            extractor.selectTrack(trackIndex);
            MediaFormat trackFormat = extractor.getTrackFormat(trackIndex);
            int muxerTrackIndex = mediaMuxer.addTrack(trackFormat, isAudio);
            int maxBufferSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            boolean inputDone = false;
            if (start > 0) {
                extractor.seekTo(start, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            } else {
                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
            long startTime = -1;

            checkConversionCanceled();

            while (!inputDone) {
                checkConversionCanceled();

                boolean eof = false;
                int index = extractor.getSampleTrackIndex();
                if (index == trackIndex) {
                    info.size = extractor.readSampleData(buffer, 0);

                    if (info.size < 0) {
                        info.size = 0;
                        eof = true;
                    } else {
                        info.presentationTimeUs = extractor.getSampleTime();
                        if (start > 0 && startTime == -1) {
                            startTime = info.presentationTimeUs;
                        }
                        if (end < 0 || info.presentationTimeUs < end) {
                            info.offset = 0;
                            info.flags = extractor.getSampleFlags();
                            if (mediaMuxer.writeSampleData(muxerTrackIndex, buffer, info, isAudio)) {
                                didWriteData(messageObject, file, false, false);
                            }
                            extractor.advance();
                        } else {
                            eof = true;
                        }
                    }
                } else if (index == -1) {
                    eof = true;
                }
                if (eof) {
                    inputDone = true;
                }
            }

            extractor.unselectTrack(trackIndex);
            return startTime;
        }
        return -1;
    }



    private void checkConversionCanceled() throws Exception {
        boolean cancelConversion = false;
        synchronized (videoConvertSync) {
            cancelConversion = cancelCurrentVideoConversion;
        }
        if (cancelConversion) {
            throw new RuntimeException("canceled conversion");
        }
    }

   
}
