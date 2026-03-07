import * as React from "react";
import { dehydrate, QueryClient, useInfiniteQuery, useQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import router from "next/router";
import { Box, Button, Grid, Link, List, ListItem, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { browsePodcasts, fetchHomePageData, fetchTopArtists, getTopScoringSongsForuser, browseStationsByCountry, getEditorsPlayList } from "../api";
import { CardsListing } from "../components/home/Carousel";
import { AppContext, AppContextType } from "./_app";
import { useSession, getSession } from "next-auth/react";
import { FeaturedCarousel2 } from "../components/home/FeaturedCarousel2";
import { History, PlayArrow, Warning } from "@mui/icons-material";
import { RadioCarousel } from "../components/radio/RadioCarousel";
import { HomePageDataType } from "../types/HomePageData.type";
import { secondsToHm, uniqueBy } from "../utils";
import { ArtistType } from "../types/ArtistType";
import { PodcastCarousel } from "../components/podcast/PodcastCarousel";
import { StreamableTracks } from "../components/StreamableTracks";
import Grid2 from "@mui/material/Unstable_Grid2";
import Image from 'next/image'
interface PageProps { }

const HomeComp: React.FC<PageProps> = ({
}: PageProps) => {
  const { data: session } = useSession()
  React.useEffect(() => {
    if (!session) {
      router.push("/auth/signin");
    }
  }, []);
  const { user, setCurrentSong } = React.useContext(
    AppContext
  ) as AppContextType;

  const {
    isSuccess,
    isLoading,
    isError,
    error,
    data,
    fetchNextPage,
    isFetchingNextPage
  } = useInfiniteQuery(['homepage'], async ({ pageParam = 1 }) => await fetchHomePageData(user?.id!, pageParam), {
    getNextPageParam: (lastPage, pages) => {
      return lastPage.page + 1
    },
    keepPreviousData: true,
    refetchOnMount: false,
    refetchOnWindowFocus: false,
    staleTime: 9000,
    cacheTime: 90000,
  })

  const { data: topStationsByVotesData, isLoading: loading0 } = useQuery(['browseStationsByCountry', 'US', 1], async () => await browseStationsByCountry('US', 1));

  const { data: editorsPlayListData, isLoading: loading234 } = useQuery(['editor_playlist'], async () => await getEditorsPlayList(10));
  const { data: topArtists, isLoading: loadingTopArtists } = useQuery(['top_artists'], async () => await fetchTopArtists());
  const { data: topScoringTracksForUser, isLoading: loadingtopTracksForuser } = useQuery(['topScoringTracksForUser'], async () => await getTopScoringSongsForuser(user?.id!, 30));
  const { data: top10PodcastsData } = useQuery(["top_podcasts"], async () => await browsePodcasts(1));

  const artCards = topArtists?.length ? uniqueBy<ArtistType>('id', topArtists)?.map(x => ({
    image: x.image,
    id: x.id,
    title: x.title,
    subtitle: '',
    type: 'artists_card',
    path: '/artist/' + x.id,
    song: null
  })) : []
  const artists: HomePageDataType = {
    cardType: 'artist_card',
    label: 'Top Artists',
    path: '/artist',
    //@ts-expect-error
    cards: artCards,
    id: 'top_artists'
  }
  console.log("topStationsByVotesData", topStationsByVotesData)
  return (
    < >

      <NoSsr>
        <Box sx={{
          overflowY: 'scroll',
          paddingX: { xs: 0, md: 3 }

        }}>
          {isError && <Typography> {'Error!' + error}</Typography>}
          {isLoading && <div>Loading...</div>}
          <FeaturedCarousel2 data={editorsPlayListData?.cards || []} />
          <RadioCarousel data={{ label: 'Listen to Top Radio Stations ', data: topStationsByVotesData?.results || [] }} />
          <Box sx={{ paddingTop: 3 }}>
            <CardsListing data={artists} key={'cardlisitng-' + 'top_artists1'} />
          </Box>
          <PodcastCarousel data={{ label: 'Top Podcasts', cards: top10PodcastsData?.results || [] }} genreid={''} />
          <Grid2 container mt={2}>
            {topScoringTracksForUser?.length && <Grid2 sx={{}}>
              <Typography variant='subtitle2' sx={{ fontVariant: 'all-small-caps' }}>Popular Tracks</Typography>
              <Table sx={{ display: { xs: 'none', md: 'block' } }}>
                <TableHead>
                  <TableRow>
                    <TableCell>#</TableCell>
                    <TableCell></TableCell>
                    <TableCell>Title</TableCell>
                    {/* <TableCell>Artits</TableCell>
                    <TableCell>Album</TableCell> */}
                    <TableCell><History fontSize="small" /></TableCell>
                  </TableRow>

                </TableHead>
                <TableBody>
                  {topScoringTracksForUser?.map((x, index) => <TableRow onClick={() => setCurrentSong(x)} key={`topScoringTracksForUser-${x.id}-${index}`}>
                    <TableCell>{index}</TableCell>
                    <TableCell sx={{ '&:hover': { cursor: 'pointer' } }}>
                      <Image alt="" src={x.thumbnail && x.thumbnail !== 'no-cover.jpg' ? x.thumbnail : "/sample.jpg"}
                        className='imsage' width={60} height={60} />
                    </TableCell>
                    <TableCell>{x.name}</TableCell>
                    {/* <TableCell>{x.artists?.map(t => <Link href={'/artist' + t.id} key={'top_songs_score_artist-' + t.id + x.id}>{t.title}</Link>)}</TableCell>
                    <TableCell>{x.album?.title}</TableCell> */}
                    <TableCell>{x.duration ? secondsToHm(x.duration / 1000) : ''}</TableCell>
                  </TableRow>)}
                </TableBody>
              </Table>

              <List className='weekly_songs' sx={{ marginBottom: '3%', display: { xs: 'block', md: 'none' } }}>
                {topScoringTracksForUser?.map((x, index) => <ListItem className="weekly_song" key={'top_songs_score' + x.id} onClick={() => setCurrentSong(x)}>
                  <div className='title'><p>{index}</p>
                    <Typography variant='subtitle2'>{x.name}</Typography>
                    <Typography variant='caption'>{x.artists?.map(t => <Link href={'/artist' + t.id} key={'top_songs_score_artist-' + t.id + x.id}>{t.title}</Link>)}</Typography>
                  </div>
                  <p><PlayArrow /></p>
                </ListItem>)}
              </List>
            </Grid2>}
            <Grid2 >
              <StreamableTracks />
            </Grid2>
          </Grid2>
          <Typography variant="caption"><Warning fontSize="small" /> Grayscaled images songs wont  play</Typography>
          {(isSuccess) && data.pages?.map((page) => page.results?.map((x, i) =>
            <Box sx={{ paddingTop: 3 }} key={x.id + i + 'cardlisitng'}>
              <CardsListing data={x} />
            </Box>
          ))}
          <div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
            <Button variant='contained' onClick={() => fetchNextPage()} size='small' sx={{ color: 'white', background: 'purple', fontWeight: 'bolder', opacity: 0.9 }}>{isFetchingNextPage ? 'Loading...' : 'Load More'}</Button>
          </div>
          <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
        </Box>

      </NoSsr>
    </>
  );
};
function moviePropsAreEqual(
  prevProps: PageProps,
  nextProps: PageProps
) {
  // //console.log("Props validation",prevProps === nextProps,prevProps ,nextProps)
  return JSON.stringify(prevProps) === JSON.stringify(nextProps);
}
export const HomePage = React.memo(
  HomeComp,
  moviePropsAreEqual
);
export default HomePage;

export const hasNavigationCSR = (next: any) => async (ctx: any) => {
  if (ctx.req.url?.startsWith('/_next')) {
    return {
      props: {},
    };
  }
  return next?.(ctx);
};

export const getServerSideProps = hasNavigationCSR(async (context: any) => {
  const session = await getSession(context)
  const queryClient = new QueryClient();
  //@ts-ignore
  const userid = session?.user?.id
  if (!queryClient.getQueryData(['homepage'])) {
    await queryClient.prefetchInfiniteQuery(["homepage"], async () => fetchHomePageData(userid, 1))
    await Promise.all([
      queryClient.prefetchQuery(['browseStationsByCountry', 'IN', 1], async () => await browseStationsByCountry('IN', 1)),
      queryClient.prefetchQuery(["editor_playlist"], async () => getEditorsPlayList(10)),
      queryClient.prefetchQuery(["top_artists"], async () => fetchTopArtists()),
      queryClient.prefetchQuery(["topScoringTracksForUser"], async () => getTopScoringSongsForuser(userid, 10)),
      queryClient.prefetchQuery(["top_podcasts"], async () => browsePodcasts(1)),
    ])
  }

  return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
})