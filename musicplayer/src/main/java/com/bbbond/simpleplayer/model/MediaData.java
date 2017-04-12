package com.bbbond.simpleplayer.model;

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

    public MediaData setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getMediaId() {
        return mediaId;
    }

    public MediaData setMediaId(String mediaId) {
        this.mediaId = mediaId;
        return this;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public MediaData setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MediaData setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public MediaData setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
        return this;
    }

    public String getDisplaySubtitle() {
        return displaySubtitle;
    }

    public MediaData setDisplaySubtitle(String displaySubtitle) {
        this.displaySubtitle = displaySubtitle;
        return this;
    }

    public String getDisplayDescription() {
        return displayDescription;
    }

    public MediaData setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
        return this;
    }

    public String getArtUri() {
        return artUri;
    }

    public MediaData setArtUri(String artUri) {
        this.artUri = artUri;
        return this;
    }

    public String getDisplayIconUri() {
        return displayIconUri;
    }

    public MediaData setDisplayIconUri(String displayIconUri) {
        this.displayIconUri = displayIconUri;
        return this;
    }

    public String getAlbum() {
        return album;
    }

    public MediaData setAlbum(String album) {
        this.album = album;
        return this;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public MediaData setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
        return this;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public MediaData setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public MediaData setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getWriter() {
        return writer;
    }

    public MediaData setWriter(String writer) {
        this.writer = writer;
        return this;
    }

    public String getComposer() {
        return composer;
    }

    public MediaData setComposer(String composer) {
        this.composer = composer;
        return this;
    }

    public String getCompilation() {
        return compilation;
    }

    public MediaData setCompilation(String compilation) {
        this.compilation = compilation;
        return this;
    }

    public String getDate() {
        return date;
    }

    public MediaData setDate(String date) {
        this.date = date;
        return this;
    }

    public long getYear() {
        return year;
    }

    public MediaData setYear(long year) {
        this.year = year;
        return this;
    }

    public long getDiscNumber() {
        return discNumber;
    }

    public MediaData setDiscNumber(long discNumber) {
        this.discNumber = discNumber;
        return this;
    }

    public String getGenre() {
        return genre;
    }

    public MediaData setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public long getTrackNumber() {
        return trackNumber;
    }

    public MediaData setTrackNumber(long trackNumber) {
        this.trackNumber = trackNumber;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public MediaData setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public long getNumTracks() {
        return numTracks;
    }

    public MediaData setNumTracks(long numTracks) {
        this.numTracks = numTracks;
        return this;
    }

    public long getBtFolderType() {
        return btFolderType;
    }

    public MediaData setBtFolderType(long btFolderType) {
        this.btFolderType = btFolderType;
        return this;
    }

    public float getRating() {
        return rating;
    }

    public MediaData setRating(float rating) {
        this.rating = rating;
        return this;
    }

    public float getUserRating() {
        return userRating;
    }

    public MediaData setUserRating(float userRating) {
        this.userRating = userRating;
        return this;
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
