package grabbers;

import com.typesafe.config.ConfigFactory;
import com.uwetrottmann.thetvdb.TheTvdb;
import com.uwetrottmann.thetvdb.entities.*;
import forms.MovieForm;
import forms.grabbers.GrabberInfoForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TheTvDbGrabber implements IInfoGrabber {

    private static final String API_KEY = "5868F2154308BB82";

    private static final String LANGUAGE = Locale.GERMAN.getLanguage();

    private final TheTvdb theTVDB;

    private final static EGrabberType TYPE = EGrabberType.THETVDB;

    private final static String IMAGE_PREFIX = ConfigFactory.load().getString("thetvdb.imagesprefix");

    private final static String ARTWORK_PREFIX = IMAGE_PREFIX + ConfigFactory.load().getString("thetvdb.artworksprefix");


    public TheTvDbGrabber() {
        theTVDB = new TheTvdb(TheTvDbGrabber.API_KEY);
    }

    @Override
    public List<GrabberSearchMovie> searchForMovie(final String searchTerm) throws GrabberException {

        final List<GrabberSearchMovie> returnVal = new ArrayList<>();

        try {

            final Call<SeriesResultsResponse> searchCall = theTVDB.search().series(searchTerm, null, null, null, TheTvDbGrabber.LANGUAGE);

            final Response<SeriesResultsResponse> response = searchCall.execute();

            if (response.isSuccessful() == false) {
                return returnVal;
            }

            final List<Series> searchSeries = response.body().data;

            if (CollectionUtils.isEmpty(searchSeries) == false) {
                for (final Series series : searchSeries) {
                    final Integer seriesId = series.id;


                    final Call<EpisodesSummaryResponse> episodesSummaryResponseCall = theTVDB.series().episodesSummary(seriesId);
                    final Response<EpisodesSummaryResponse> episodesSummaryResponse = episodesSummaryResponseCall.execute();
                    if (episodesSummaryResponse.isSuccessful() == false) {
                        continue;
                    }

                    final EpisodesSummary episodesSummary = episodesSummaryResponse.body().data;
                    final List<Integer> airedSeasons = episodesSummary.airedSeasons;

                    for (final Integer airedSeason : airedSeasons) {
                        final String movieTitle = buildMovieName(series, airedSeason);

                        final String systemId = seriesId + "_" + airedSeason;
                        final GrabberSearchMovie searchMovie = new GrabberSearchMovie(systemId, movieTitle, IMAGE_PREFIX + series.poster, TheTvDbGrabber.TYPE);
                        returnVal.add(searchMovie);

                    }
                }
            }


        } catch (IOException e) {
            if (Logger.isErrorEnabled() == true) {
                Logger.error("An error happened while searching for movie : " + searchTerm + " in : " + TheTvDbGrabber.class.getName(), e);
            }
        }

        return returnVal;
    }

    private String buildMovieName(final Series series, final int seasonId) {
        return series.seriesName + " Season: " + seasonId;
    }

    @Override
    public GrabberDisplayMovie getDisplayMovie(final String id) throws GrabberException {

        try {


            final String[] split = id.split("_");
            final int seriesId = Integer.parseInt(split[0]);
            final int seasonId = Integer.parseInt(split[1]);

            final Series series = getSeriesInfo(seriesId);

            final List<SeriesImagesQueryParam> imagesQueryParams = theTVDB.series().imagesQueryParams(seriesId).execute().body().data;

            final List<GrabberImage> posterSeasonList = getSeasonPosterImages(seriesId, imagesQueryParams, true);

            final List<GrabberImage> backdrops = getGrabberImages(seriesId, ETheTvDbImageType.FANART, true, imagesQueryParams);


            return new GrabberDisplayMovie(id, buildMovieName(series, seasonId), series.overview, posterSeasonList, backdrops, new ArrayList<>(), TheTvDbGrabber.TYPE, series.imdbId);
        } catch (IOException e) {
            if (Logger.isErrorEnabled() == true) {
                Logger.error("An error happened while displaying movie : " + id + " in : " + TheTvDbGrabber.class.getName(), e);
            }
            return null;
        }
    }

    private List<GrabberImage> getSeasonPosterImages(final int seriesId, final List<SeriesImagesQueryParam> imagesQueryParams, final boolean asThumbNail) throws IOException, GrabberException {
        final List<GrabberImage> posterList = getGrabberImages(seriesId, ETheTvDbImageType.POSTER, asThumbNail, imagesQueryParams);
        final List<GrabberImage> seasonList = getGrabberImages(seriesId, ETheTvDbImageType.SEASON, asThumbNail, imagesQueryParams);
        final List<GrabberImage> posterSeasonList = new ArrayList<>();
        posterSeasonList.addAll(posterList);
        posterSeasonList.addAll(seasonList);
        return posterSeasonList;
    }

    /**
     * Gets the series info for the given series id
     *
     * @param seriesId
     * @return
     * @throws IOException
     * @throws GrabberException
     */
    private Series getSeriesInfo(int seriesId) throws IOException, GrabberException {
        final Response<SeriesResponse> seriesResponse = theTVDB.series().series(seriesId, TheTvDbGrabber.LANGUAGE).execute();
        if (seriesResponse.isSuccessful() == false) {
            final String message = "Could not find series: " + seriesId;
            Logger.error(message);
            throw new GrabberException(message);
        }

        final Series series = seriesResponse.body().data;
        return series;
    }


    /**
     * Gets all images for the given series
     *
     * @param seriesId
     * @param imageType
     * @param imagesQueryParams
     * @return
     * @throws IOException
     * @throws GrabberException
     */
    private List<GrabberImage> getGrabberImages(int seriesId, final ETheTvDbImageType imageType, final boolean asThumbNail, final List<SeriesImagesQueryParam> imagesQueryParams) throws IOException, GrabberException {

        // check if the image type is supported
        boolean imageTypeSupported = imagesQueryParams.stream().anyMatch(imagesQueryParam -> imagesQueryParam.keyType.equals(imageType.type));
        if (imageTypeSupported == false) {
            Logger.info("Could not find image for series:" + seriesId + " imagetype: " + imageType);
            return new ArrayList<>();
        }

        final Response<SeriesImageQueryResultResponse> imageResponse = theTVDB.series().imagesQuery(seriesId, imageType.type, null, null, TheTvDbGrabber.LANGUAGE).execute();
        if (imageResponse.isSuccessful() == false) {
            final String message = "Could not find images for series: " + seriesId + " type: " + imageType;
            Logger.error(message);
            throw new GrabberException(message);
        }


        final List<SeriesImageQueryResult> imageQueryResultList = imageResponse.body().data;
        final List<GrabberImage> imageList = new ArrayList<>();
        for (final SeriesImageQueryResult seriesImageQueryResult : imageQueryResultList) {

            if (asThumbNail) {
                imageList.add(new GrabberImage(String.valueOf(seriesImageQueryResult.id), getThumbUrl(seriesImageQueryResult)));
            } else {
                imageList.add(new GrabberImage(String.valueOf(seriesImageQueryResult.id), ARTWORK_PREFIX + seriesImageQueryResult.fileName));
            }


        }
        return imageList;
    }

    private String getThumbUrl(final SeriesImageQueryResult imageInfo) {
        return ARTWORK_PREFIX + ((StringUtils.isEmpty(imageInfo.thumbnail)) ? imageInfo.fileName : imageInfo.thumbnail);
    }

    @Override
    public MovieForm fillInfoToMovieForm(final GrabberInfoForm grabberInfoForm) throws GrabberException {

        try {
            final String id = grabberInfoForm.grabberMovieId;

            final String[] split = id.split("_");
            final int seriesId = Integer.parseInt(split[0]);
            final String seasonId = split[1];

            final Series series = getSeriesInfo(seriesId);

            final MovieForm movieForm = new MovieForm();

            final Integer season = Integer.valueOf(seasonId);

            movieForm.title = buildMovieName(series, season);
            movieForm.plot = series.overview;
            movieForm.series = series.seriesName;
            movieForm.imdbId = series.imdbId;
            movieForm.grabberType = TheTvDbGrabber.TYPE;
            movieForm.grabberId = grabberInfoForm.grabberMovieId;
            movieForm.runtime = Integer.valueOf(series.runtime);

            final String firstAired = series.firstAired;
            if (StringUtils.isEmpty(firstAired) == false) {
                final String[] split2 = firstAired.split("-");
                if (split2.length == 3) {
                    movieForm.year = Integer.valueOf(split2[0]);
                }
            }

            movieForm.genres.addAll(series.genre);

            final Response<ActorsResponse> actorsResponse = theTVDB.series().actors(seriesId).execute();
            if (actorsResponse.isSuccessful() == true) {
                final List<Actor> actors = actorsResponse.body().data;
                if (CollectionUtils.isNotEmpty(actors) == true) {
                    for (final Actor actor : actors) {
                        movieForm.actors.add(actor.name);
                    }
                }
            } else {
                if (Logger.isWarnEnabled() == true) {
                    Logger.warn("Could not fetch actors for series: " + seriesId + " " + actorsResponse.message());
                }
            }

            final List<SeriesImagesQueryParam> imagesQueryParams = theTVDB.series().imagesQueryParams(seriesId).execute().body().data;
            final List<GrabberImage> posterSeasonList = getSeasonPosterImages(seriesId, imagesQueryParams, false);
            final List<GrabberImage> backdrops = getGrabberImages(seriesId, ETheTvDbImageType.FANART, false, imagesQueryParams);

            posterSeasonList.stream().filter(grabberImage -> grabberImage.id.equals(grabberInfoForm.grabberPosterId)).findFirst().ifPresent(grabberImage -> {movieForm.posterUrl =  grabberImage.url;});
            backdrops.stream().filter(grabberImage -> grabberImage.id.equals(grabberInfoForm.grabberBackDropId)).findFirst().ifPresent(grabberImage -> {movieForm.backDropUrl = grabberImage.url;});

            return movieForm;
        } catch (IOException e) {
            if (Logger.isErrorEnabled() == true) {
                Logger.error("An error happened while filling in the movieform in : " + TheTvDbGrabber.class.getName(), e);
            }
            return null;
        }

    }
}