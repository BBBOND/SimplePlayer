package com.kim.simpleplayer.model;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;

/**
 * 媒体数据存放类
 * Created by kim on 2017/2/14.
 */

public class MediaData implements MediaDataSourceI {

    String mediaId, mediaUri, title, displayTitle, displaySubtitle, displayDescription,
            artUri, displayIconUri, album, albumArtist, albumArtUri, artist, author,
            writer, composer, compilation, date, genre;
    long duration, numTracks, btFolderType, trackNumber, discNumber, year;
    float rating, userRating;

    public MediaData() {
    }

    public MediaData(Bundle bundle) {
        // *id
        mediaId = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        // *media的路径
        mediaUri = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        // *标题
        title = bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        // *显示的标题
        displayTitle = bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
        // *显示的副标题
        displaySubtitle = bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE);
        // 描述
        displayDescription = bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION);
        // 时长
        duration = bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        // media插图的路径
        artUri = bundle.getString(MediaMetadataCompat.METADATA_KEY_ART_URI);
        // 显示小图标的路径
        displayIconUri = bundle.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI);
        // 专辑名
        album = bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        // 专辑封面
        // 专辑艺术家
        albumArtist = bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST);
        // 专辑封面路径
        albumArtUri = bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        // *歌手
        artist = bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        // 作者(原唱?)
        author = bundle.getString(MediaMetadataCompat.METADATA_KEY_AUTHOR);
        // 作词
        writer = bundle.getString(MediaMetadataCompat.METADATA_KEY_WRITER);
        // 作曲
        composer = bundle.getString(MediaMetadataCompat.METADATA_KEY_COMPOSER);
        // 蓝牙AVRCP 1.5的第6.10.2.2节中指定的媒体的蓝牙文件夹类型
        btFolderType = bundle.getLong(MediaMetadataCompat.METADATA_KEY_BT_FOLDER_TYPE);
        // media的编辑状态
        compilation = bundle.getString(MediaMetadataCompat.METADATA_KEY_COMPILATION);
        // media的创建或发布时间
        date = bundle.getString(MediaMetadataCompat.METADATA_KEY_DATE);
        // media创建或发布的时长
        year = bundle.getLong(MediaMetadataCompat.METADATA_KEY_YEAR);
        // 光盘号
        discNumber = bundle.getLong(MediaMetadataCompat.METADATA_KEY_DISC_NUMBER);
        // 类型，流派
        genre = bundle.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
        // 媒体的原始来源中的曲目数
        numTracks = bundle.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS);
        // 曲目编号
        trackNumber = bundle.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
    }

    @Override
    public MediaMetadataCompat getMediaMetadata() {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, displayTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, displaySubtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, displayDescription)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artUri)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, displayIconUri)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, albumArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, author)
                .putString(MediaMetadataCompat.METADATA_KEY_WRITER, writer)
                .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, composer)
                .putString(MediaMetadataCompat.METADATA_KEY_COMPILATION, compilation)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, date)
                .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, year)
                .putLong(MediaMetadataCompat.METADATA_KEY_DISC_NUMBER, discNumber)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, numTracks)
                .putLong(MediaMetadataCompat.METADATA_KEY_BT_FOLDER_TYPE, btFolderType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.putRating(MediaMetadataCompat.METADATA_KEY_RATING, RatingCompat.newStarRating(RatingCompat.RATING_5_STARS, rating))
                    .putRating(MediaMetadataCompat.METADATA_KEY_USER_RATING, RatingCompat.newStarRating(RatingCompat.RATING_5_STARS, userRating));
        }

        return builder.build();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDisplaySubtitle() {
        return displaySubtitle;
    }

    public void setDisplaySubtitle(String displaySubtitle) {
        this.displaySubtitle = displaySubtitle;
    }

    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    public String getArtUri() {
        return artUri;
    }

    public void setArtUri(String artUri) {
        this.artUri = artUri;
    }

    public String getDisplayIconUri() {
        return displayIconUri;
    }

    public void setDisplayIconUri(String displayIconUri) {
        this.displayIconUri = displayIconUri;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getCompilation() {
        return compilation;
    }

    public void setCompilation(String compilation) {
        this.compilation = compilation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getYear() {
        return year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    public long getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(long discNumber) {
        this.discNumber = discNumber;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(long trackNumber) {
        this.trackNumber = trackNumber;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getNumTracks() {
        return numTracks;
    }

    public void setNumTracks(long numTracks) {
        this.numTracks = numTracks;
    }

    public long getBtFolderType() {
        return btFolderType;
    }

    public void setBtFolderType(long btFolderType) {
        this.btFolderType = btFolderType;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }

    @Override
    public String toString() {
        return "MediaData{" +
                "mediaId='" + mediaId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", title='" + title + '\'' +
                ", displayTitle='" + displayTitle + '\'' +
                ", displaySubtitle='" + displaySubtitle + '\'' +
                ", displayDescription='" + displayDescription + '\'' +
                ", artUri='" + artUri + '\'' +
                ", displayIconUri='" + displayIconUri + '\'' +
                ", album='" + album + '\'' +
                ", albumArtist='" + albumArtist + '\'' +
                ", albumArtUri='" + albumArtUri + '\'' +
                ", artist='" + artist + '\'' +
                ", author='" + author + '\'' +
                ", writer='" + writer + '\'' +
                ", composer='" + composer + '\'' +
                ", compilation='" + compilation + '\'' +
                ", date='" + date + '\'' +
                ", year='" + year + '\'' +
                ", discNumber='" + discNumber + '\'' +
                ", genre='" + genre + '\'' +
                ", trackNumber='" + trackNumber + '\'' +
                ", duration=" + duration +
                ", numTracks=" + numTracks +
                ", btFolderType=" + btFolderType +
                ", rating=" + rating +
                ", userRating=" + userRating +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaData mediaData = (MediaData) o;

        if (!mediaId.equals(mediaData.mediaId)) return false;
        if (!title.equals(mediaData.title)) return false;
        if (!displayTitle.equals(mediaData.displayTitle)) return false;
        return artist.equals(mediaData.artist);

    }

    @Override
    public int hashCode() {
        int result = mediaId.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + displayTitle.hashCode();
        result = 31 * result + artist.hashCode();
        return result;
    }
}
