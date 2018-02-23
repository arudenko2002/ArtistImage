package ArtistImageStudy;

import okhttp3.*;
import okhttp3.OkHttpClient.Builder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ReadHTTP {
    static String auth = "Bearer BQA-8Tc-ktLSjuJc13M-4MqYt3FJE2SFCKH-QVA4N48IYkg08eEnUWgFzAdEnPN8lPsbubOfsC_FbZu6Vo7qoA";

    String track_image = "";
    String artist_image = "";
    String url="https://api.spotify.com/v1/tracks/";
    String search="https://api.spotify.com/v1/search";
    int verbocity=1;

    public ReadHTTP(int verbocity) {
        this.verbocity=verbocity;
    }

    private String getBody(String href) throws Exception {
        String body = "";
        int counter=0;
        while(true) {
            counter++;
            if(counter>10) {
                System.out.println("The call was done "+counter+" times without success. exiting...");
                return "";
            }
            OkHttpClient client = new OkHttpClient.Builder()
                    .authenticator(new Authenticator() {
                        @Override
                        public Request authenticate(Route route, Response response) throws IOException {
                            return response.request().newBuilder()
                                    .header("Authorization", auth)
                                    .build();
                        }
                    })
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build();
            //System.out.println(client);
System.out.println("href to fail="+href);
            Request request = new Request.Builder()
                    .url(href)
                    .get()
                    .addHeader("content-type", "application/json; charset=UTF-8")
                    .addHeader("authorization", auth)
                    .build();
            //System.out.println(request);
            try {
                //System.out.println("API request to spotify");
                Response response = client.newCall(request).execute();
                //System.out.println("Code="+response.code());
                body = response.body().string();
                Thread.sleep(2 * 1000);
                break;
            } catch (InterruptedException e) {
                System.out.println("Interrupted Exception!!!");
                e.printStackTrace();
            } catch (java.net.ProtocolException e) {
                synchronized(this) {
                    RefreshToken rt = new RefreshToken();
                    auth = "Bearer " + rt.refreshToken();
                    System.out.println("_TOKEN=" + auth);
                }
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("Socket Timeout!!!");
                e.printStackTrace();
            } catch (java.net.SocketException e) {
                System.out.println("Socket Exception!!!");
                e.printStackTrace();
            }
        }
        return body;
    }

    private String getTrackImageBody(String body) {
        int resolution=2; //The last one in the list of 3, the smallest
        int TRACKS=1;
        JSONObject b = new JSONObject(body);
        if(!b.isNull("album") &&!b.getJSONObject("album").isNull("images")&&!b.getJSONObject("album").getJSONArray("images").isNull(2)) {
            JSONArray images = b.getJSONObject("album").getJSONArray("images");
            if(images.isNull(0)) {
                System.out.println("No artist image found!");
                return "various artists";
            } else {
                int imagesn=images.length();
                if(imagesn>1) {
                    resolution=imagesn-TRACKS;
                } else {
                    resolution=0;
                }
                JSONObject image = images.getJSONObject(resolution);
                return image.getString("url");
            }

        }else {
            return "no track/album image uri";
        }
    }

    private String getArtistHref(String body,String artist_name) {
        JSONObject b = new JSONObject(body);
        if(!b.isNull("artists")&& !b.getJSONArray("artists").isNull(0)) {
            JSONArray artists = b.getJSONArray("artists");
            int artist_index=getArtistIndex(artists,artist_name);
            JSONObject href = artists.getJSONObject(artist_index);
            System.out.println("AAAAAAAAAAA=="+href.getString("name"));
            System.out.println("BBBBBBBBBBB=="+artist_name);
            return href.getString("href");
        } else {
            return "no artist image uri";
        }
    }

    private int getArtistIndex(JSONArray artists,String artist_name) {
        int result=0;
        if(artist_name.length()==0)return result;

        double similarity=0.0;
        for(int i=0; i<artists.length();i++) {
            JSONObject href = artists.getJSONObject(i);
            String name=href.getString("name");
            double sim = getSimilarity(artist_name, name);
            if(similarity<sim) {
                similarity=sim;
                result=i;
            }
        }
        System.out.println("SIMILARITY="+similarity);
        return result;
    }

    private double getSimilarity(String n1, String n2) {
        String name1=n1.toLowerCase();
        String name2=n2.toLowerCase();
        if(name1.equals(name2)) return 1.0;
        String[] s1 = (name1.replace(".","").split(" "));
        String[] s2 = (name2.replace(".","").split(" "));
        if(s1.length==1 && s2.length==1) {
            return getDiff(s1[0],s2[0]);
        }
        if(s1.length==2 && s2.length==3) {
            if(s1[0].equals(s2[0]) && s1[1].equals(s2[2])) return 0.9;
            return getDiff(s1[0],s2[0])*getDiff(s1[1],s2[2]);
        }
        if(s1.length==3 && s2.length==2) {
            if(s2[0].equals(s1[0]) && s1[2].equals(s2[1])) return 0.9;
            return getDiff(s1[0],s2[0])*getDiff(s1[2],s2[1]);
        }

        if(s1.length==3 && s2.length==3) {
            if(s2[0].equals(s1[0]) && s1[2].equals(s2[2])) return 1.0;
            return getDiff(s1[0],s2[0])*getDiff(s1[2],s2[2]);
        }

        return 0.0;
    }

    private char[] sort(String s) {
        char tempArray[] = s.toCharArray();
        Arrays.sort(tempArray);
        return tempArray;
    }

    private double getDiff(String s1, String s2) {
        //System.out.println("1="+s1);
        //System.out.println("2="+s2);
        char[] ss1 = sort(s1);
        char[] ss2 = sort(s2);
        int och=0;
        int j=0;
        int i=0;
        while(true) {
            //System.out.println("i="+i+" ss1[i]="+ss1[i]+"   j="+j+" ss2="+ss2[j]);
            if(i==8) {
                int k=0;
            }
            if(ss1[i]==ss2[j]) {
               och++;
               i++;
               j++;
               if(ss1.length==i || ss2.length==j) break;
               continue;
            }
            if(ss1[i]>ss2[j]) {
                j++;
                if(ss2.length==j) break;
            }
            if(ss1[i]<ss2[j]) {
                i++;
                if(ss1.length==i) break;
            }
        }
        return 1.0*och/ss1.length*1.0*och/ss2.length;
    }

    private String getArtistImageBody(String body) {
        int ARTISTS=1; //choose artist before the last one in the list of 3
        int resolution = 2;//the last one in the list(smallest)
        //System.out.println("Artist image body="+body);
        JSONObject b = new JSONObject(body);
        if(b.isNull("images")) {
            System.out.println("No Section IMAGES at all!!");
            //System.exit(200);
            return "various artists";
        }
        JSONArray images = b.getJSONArray("images");

        if(images.isNull(0)) {
            System.out.println("No artist image found!");
            return "various artists";
        } else {
            int imagesn=images.length();
            if(imagesn>1) {
                resolution=imagesn-ARTISTS;
            } else {
                resolution=0;
            }
            JSONObject href = images.getJSONObject(resolution);
            return href.getString("url");
        }
    }

    public void getImages(String uri, String name) throws Exception{
        // Read track data
        String track_json_body = getBody(url+uri);
        // Get track image
        track_image = getTrackImageBody(track_json_body);
        // Get artist href from track data
        String artist_href = getArtistHref(track_json_body,name);
        // Read arstist data
        if(!artist_href.startsWith("no ")) {
            String artist_json_body = getBody(artist_href);
            // Get artist image url
            artist_image = getArtistImageBody(artist_json_body);
        } else {
            System.out.println("no artist found");
        }
    }

    public void test() throws Exception {
        ArrayList<String> ar= new ArrayList<String>();
        ar.add("spotify:track:05zvi8nm6Z7AHX3wOKxcva");
        ar.add("spotify:track:0PrLodAzseOnN1EACxuAfV");
        //ar.add("spotify:track:0eK5y1c7WDyWwx6k9SFjbT");
        //ar.add("spotify:track:0qXdJdBpOdqm1x70L5VxjG");
        //ar.add("spotify:track:386i986Vk3oWiUgwRU5vV2");
        //ar.add("spotify:track:77AJ7lv6iU2weqjX6lEx4E");
        //ar.add("spotify:track:5ASZFbCod633wLragmPYTj");
        //ar.add("spotify:track:4RmU5uXxD5JzBc1wfBK6Gb");
        //ReadHTTP rhp = new ReadHTTP();
        for(int i=0; i<ar.size();i++) {
            String http="https://api.spotify.com/v1/tracks/"+ar.get(i).split(":")[2];
            System.out.println(http);
            String body = getBody(http);
            System.out.println("Track body="+body);
            String href = getTrackImageBody(body);
            System.out.println("HREF track image="+href);
            String artisthref = getArtistHref(body,"");
            System.out.println("HREF artist="+artisthref);
            String ab = getBody(artisthref);
            System.out.println("Artist body="+ab);
            String aimage = getArtistImageBody(ab);
            System.out.println("HREF artist image="+aimage);
        }
        System.exit(-1);
    }

    public void test2() throws Exception {
        //rhp.getImages("568BqBOqxp0xyv93dmjv3Q");
        String body = getBody("https://api.spotify.com/v1/tracks/568BqBOqxp0xyv93dmjv3Q");
        System.out.println("AAAAAAAA="+body);
        String href = getTrackImageBody(body);
        System.out.println("GGGGGGGGGGGtrack="+href);
        //String ss  = rhp.getBody(href);
        //System.out.println("SSSSSSS==="+ss);
        String ar = getArtistHref(body,"");
        System.out.println("TTTTTTTT"+ar);
        String dd = getBody(ar);
        System.out.println(dd);
        String arrr = getArtistImageBody(dd);
        System.out.println("GGGGGGGGGGGGGartist="+arrr);
        System.out.println("\n\n\n");

    }

    public void search_track(String href_track) throws Exception{
            System.out.println("***** HREF_TRACK="+href_track);
            String body = getBody(href_track);
            if(body.contains("\"status\" : 400,")) {
                System.out.println("No artists available in this JSON!");
                return;
            }
            if(verbocity>1)
                System.out.println("TRACK BODY(my way)"+body);
            String href = getTrackImageBody(body);
            if(verbocity>0)
                System.out.println("***** IMAGE TRACK="+href);
            String ar = getArtistHref(body,"");
            if(verbocity>0)
                System.out.println("***** HREF_ARTIST="+ar);

            String dd = getBody(ar);
            if(verbocity>1)
                System.out.println("ARTIST BODY(my way)"+dd);
            String artist_image = getArtistImageBody(dd);
            System.out.println("******************** IMAGE ARTIST="+artist_image);
            //if(!artist_image.equals("https://i.scdn.co/image/c55bc6f57b6bb297425c3ae694f92672dcf0e2c2")) {
            //    System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
            //}
            if(!artist_image.equals("https://i.scdn.co/image/a40e63ac6de01a8a610a38f2d732d7f83d5ea689")) {
                System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
            }
            //System.out.println("\n\n\n");
    }

    public String search_isrc(String isrc) throws Exception{

        String market=isrc.substring(0,2);
        //System.out.println(market);
        //System.exit(0);
        String url = search+"?"+"q=isrc:"+isrc+"&type=track&offset=0&limit=20";
        if(market.matches("^[A-Z]+$")) {
            url=url+"&market="+market;
            System.out.println(url);
        }
        //String url=search+"?"+"q=isrc:"+isrc+"&type=track&offset=0&limit=20";
        String body = getBody(url);
        if(verbocity>1)
            System.out.println("TRACK BODY(Dmitri)"+body)  ;
        try {
            JSONObject b = new JSONObject(body);
            int totals = b.getJSONObject("tracks").getBigInteger("total").intValue();
            if (totals == 0) {
                return "";
            } else {
                JSONArray tracks = b.getJSONObject("tracks").getJSONArray("items");
                for (int i = 0; i < totals; i++) {

                    JSONObject track = tracks.getJSONObject(i);
                    String track_name = track.getString("name");
                    String track_href = track.getString("href");
                    JSONObject images = track.getJSONObject("album").getJSONArray("images").getJSONObject(0);
                    String image = images.getString("url");
                    if(verbocity>0) {
                        System.out.println("START OF ITEM TRACK_NAME=" + track_name + "   N=" + i);
                        System.out.println("TRACK_HREF=" + track_href);
                        System.out.println("******************** TRACK IMAGE=" + image);
                    }
                    //search_track(track_href);
                    //ARTISTS
                    JSONArray artists = track.getJSONArray("artists");
                    for (int j = 0; j < artists.length(); j++) {
                        JSONObject artist = artists.getJSONObject(j);
                        String artist_href = artist.getString("href");
                        if(verbocity>0)
                            System.out.println("ARTIST_HREF=" + artist_href);
                        String artist_name = artist.getString("name");
                        if(verbocity>0)
                            System.out.println("ARTIST_NAME=" + artist_name);
                        search_artist(artist_href,j);
                    }
                    if(verbocity>0)
                    System.out.println("END OF ITEM=" + track_name+"\n\n");
                }
            }
        } catch (JSONException e) {
            System.out.println("ERROR: "+body);
            System.out.println(e.getMessage());
            System.out.println(e.fillInStackTrace());
        }
        return body;
    }

    public void search_artist(String artist_href, int number) throws Exception {

        if(verbocity>0)
            System.out.println("oooooo ARTIST_HREF="+artist_href);
        String body = getBody(artist_href);
        if(verbocity>1)
            System.out.println("ARTIST_BODY(Dmitri way)="+body);

        JSONObject b = new JSONObject(body);
        if(verbocity>1)
            System.out.println("ooooo ARTIST_NAME="+b.getString("name"));
        if(b.isNull("images")|| b.getJSONArray("images").length()==0) {
            System.out.println("Artist record does not contain images:\n"+body);
            return;
        }
        JSONArray images = b.getJSONArray("images");
        String artist_image = images.getJSONObject(images.length()-1).getString("url");
        if(verbocity>0)
            System.out.println("******************** IMAGE_ARTIST="+artist_image);
        //if(!artist_image.equals("https://i.scdn.co/image/c55bc6f57b6bb297425c3ae694f92672dcf0e2c2")&& number==0) {
        //    System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
        //}
        //if(!artist_image.equals("https://i.scdn.co/image/a40e63ac6de01a8a610a38f2d732d7f83d5ea689")&& number==0) {
        //    if(verbocity>0)
        //        System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
        //}
    }

    public static void main(String[] args) throws Exception {
        ReadHTTP rhp = new ReadHTTP(2);
        //System.out.println(rhp.getDiff("Töpper, Hertha","Hertha Töpper"));
        //System.exit(0);
        //rhp.test();
        //rhp.test2();
        //rhp.search_track("https://api.spotify.com/v1/tracks/568BqBOqxp0xyv93dmjv3Q");
        //rhp.search_isrc("USEE10001993");
        //rhp.search_isrc("USDJ20010662");");
        //rhp.search_isrc("DEUL31700007");

        //rhp.search_isrc("USDJM0400007
        //rhp.search_track( "https://api.spotify.com/v1/tracks/1lG4XOmTzTdfHyp3VP3uS9");

        //rhp.search_artist("https://api.spotify.com/v1/artists/2QYEvpsWUOjqaYuxDPTCmV");
        //rhp.search_track("https://api.spotify.com/v1/search?query=isrc%3ADEUL31700021&type=track&offset=0&limit=20");
        //String isrc="";
        //Arhp.search_track("https://api.spotify.com/v1/search?type=track&q=isrc:DEUL31700007");
        //rhp.search_track("https://open.spotify.com/track/75O5uX5InQEvnboErf28GC");
        //rhp.search_track("https://api.spotify.com/v1/tracks/4N9LNoDqq3iBHkfUh8FyiB");
        String h="";
        h = "4N9LNoDqq3iBHkfUh8FyiB";
        //h = "75O5uX5InQEvnboErf28GC";
        h = "4eh0ZnjqYVs5RDc4UZ8pu6";
        //h = "2PryGKyQbkK7qZLMl1BYi0";
        //h = "4PnZoaeUqZf96NoUTOb52D";
        //rhp.search_track("https://api.spotify.com/v1/tracks/"+h);
        String playlist="https://api.spotify.com/v1/users/spotify/playlists/0GnFFumGQDTZxfj0Y4N7Kr";
        //playlist="https://api.spotify.com/v1/users/spotify/playlists/59ZbFPES4DQwEjBpWHzrtC";
        //playlist="https://api.spotify.com/v1/users/spotify/playlists/12R8HZh3GHUw1c4sgPtu6x";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/19Hsw3I1EALtwkdimI8OUK";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/37i9dQZF1E9hu0JEvOkNpe";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/37i9dQZEVXbMDoHDwVN2tF";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/2yDYrySCmg2RZmnOZ94S63";
        //spotify:user:1292469929:playlist:0iV5CVqDtrzw7UBkakxVrR
        playlist="https://api.spotify.com/v1/users/128509955/playlists/0Oge6AjXcvHpe0I7ShY3E2";
        playlist="https://api.spotify.com/v1/users/p3.no/playlists/5tZVK9cAWuMmQ3KUQYPItw";
        //spotify:user:spotify:playlist:37i9dQZF1E9hu0JEvOkNpe
        playlist="https://api.spotify.com/v1/users/spotify/playlists/37i9dQZF1E9hu0JEvOkNpe";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/37i9dQZF1DX92MLsP3K1fI";
        playlist="https://api.spotify.com/v1/users/spotifybrazilian/playlists/6y23fI1axTBfSSS1iVu2q0";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/37i9dQZF1DX6aTaZa0K6VA";
        playlist="https://api.spotify.com/v1/users/spotify/playlists/37i9dQZF1DWSVtp02hITpN";
        playlist="https://api.spotify.com/v1/users/billboard.com/playlists/6UeSakyzhiEt4NB3UAd6NQ";
        playlist="https://api.spotify.com/v1/tracks/5NoVG5fkrxBtx2BJvq0knu";
        playlist="https://api.spotify.com/v1/tracks/0UXpYQ877cgtuHgPNMPJgx";
        playlist="https://api.spotify.com/v1/tracks/2IDHCNFBTUrNzppadVeW0g";
        playlist="https://api.spotify.com/v1/tracks/67PxDyx067lEJE5FqjzKWc";
        playlist="https://api.spotify.com/v1/tracks/3X38ErFiKgzUxinBlhwuWm";
        playlist="https://api.spotify.com/v1/tracks/3cGuiySfwJPXqvE15OiEEg";
        playlist="https://api.spotify.com/v1/tracks/0TxLMunWzqhdv0wwldLaD2";
        playlist="https://api.spotify.com/v1/tracks/1ds5oarhQjo0K9gNB8SoP1";
        playlist="https://api.spotify.com/v1/tracks/4CTHtnE1XGrDVgRCFEPczW";
        playlist="https://api.spotify.com/v1/tracks/6ys9oyFvw7FXbs5UMZ7I7s";
        //rhp.search_artist("https://api.spotify.com/v1/artists/6S2OmqARrzebs0tKUEyXyp",0);
        //rhp.search_artist("https://api.spotify.com/v1/artists/16HzwwD4w8AQTPf0IpEu8G",0);
        //rhp.search_artist("https://api.spotify.com/v1/artists/16HzwwD4w8AQTPf0IpEu8G",0);
        //rhp.search_artist("https://api.spotify.com/v1/artists/1HxJeLhIuegM3KgvPn8sTa",0);
        //rhp.search_artist("https://api.spotify.com/v1/artists/4zsTq27Sde0ucZgKiflRLR",0);
        playlist="https://api.spotify.com/v1/tracks/7hiNLBDjY9vagdugyRqqSI";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:7BOVPQ5RoyYnHkYQRVkZXl";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:3jNyatJv1JxuIlwKoYlo7Y";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:7hI3AkJXNxPzvXrU3pr2qW";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:6ydoSd3N2mwgwBHtF6K7eX";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:2AlSwDHosDf3eJxzgODrVu";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:1O10apSOoAPjOu6UhUNmeI";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:1fWhLdTbmyt4jJxxHBSI6P";
        playlist="https://api.spotify.com/v1/tracks/spotify:track:2E1V6nbjINpLpiMaWMSYZ4";
        //playlist="https://api.spotify.com/v1/tracks/2K98H2lHLQltKUD8hskFgw";
        //playlist="https://api.spotify.com/v1/tracks/4wODOg3NPt56JE0sB6d5hY";

        rhp.search_track(playlist);
        //rhp.getImages("0UXpYQ877cgtuHgPNMPJgx","stepan p. blok");
        //System.exit(0);
        //playlist="https://api.spotify.com/v1/tracks/5V4fmMporHb1atgiaJJCcY";
        //playlist="https://api.spotify.com/v1/tracks/57ef8SNWFJrkyS6eIuoQVp";

        //String body = rhp.getBody(playlist);
        //System.out.println("BODY="+body);

    }
}
