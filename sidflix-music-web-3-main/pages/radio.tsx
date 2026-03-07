import NoSsr from "@mui/material/NoSsr";
import router from "next/router";
import { AppContext, AppContextType } from "./_app";
import { getSession, useSession } from "next-auth/react";
import { Box, Typography } from "@mui/material";
import { dehydrate, QueryClient, useQuery, useQueryClient } from "react-query";
import { browseStationsByCountry, getAllGenres, getAllRadioCountries, getLastPlayedStations, trendingStations, userLikedStatison } from "../api";
import { RadioCarousel } from "../components/radio/RadioCarousel";
import RadioCountries from "../components/radio/CountryAutoComplete";
import RadioGenreTabs from "../components/radio/RadioGenreTabs";
import { hasNavigationCSR } from ".";
import { FC, useEffect, useContext, useState, memo } from "react";
interface PageProps {
}

const RadioComp: FC<PageProps> = ({
}: PageProps) => {
  const queryClient = useQueryClient()
  const { data: session } = useSession()
  useEffect(() => {
    if (!session) {
      router.push("/auth/signin");
    }
  }, []);

  const { user, setStations } = useContext(
    AppContext
  ) as AppContextType;
  const [country, setCountry] = useState<string>('US');
  const { data: countries, isLoading: loading1 } = useQuery(["all_countries"], async () => await getAllRadioCountries());
  const { data: genres, isLoading: loading2 } = useQuery(["all_station_genres", country], async () => await getAllGenres(country));
  const { data: lastPlayedStations, isLoading: loading8 } = useQuery(['lastPlayedStations', user?.id], async () => await getLastPlayedStations(user?.id!));
  const { data: trendingStationsData, isLoading: loading5 } = useQuery(['trendingStations', country], async () => await trendingStations(country));
  const { data: userLikedStatisonData, isLoading: loading6 } = useQuery(['userlikedStations', user?.id], async () => await userLikedStatison(user?.id!));
  const { data: topStationsByVotes, isLoading: loading0 } = useQuery(['browseStationsByCountry', country, 1], async () => await browseStationsByCountry(country, 1));
  useEffect(() => {
    console.log("COuntry changed", country)
    queryClient.invalidateQueries(['topStationsByClickCount', country])
    queryClient.invalidateQueries(['browseStationsByCountry', country])
  }, [country])
  useEffect(() => {
    setStations([...userLikedStatisonData || [], ...topStationsByVotes?.results || [], ...trendingStationsData || []])
  }, [!loading1 && !loading2 && !loading0 && !loading5 && !loading6 && !loading8])

  // if (loading1 || loading2 || loading0 || loading5 || loading6) return <h4>Loading....</h4>
  return (
    <NoSsr>
      <Box sx={{
        overflowY: 'scroll',
        overflowX: 'hidden',
        marginTop: 2
      }}>
        {countries && <RadioCountries genres={countries} setCountry={setCountry} />}
        <RadioCarousel data={{ label: 'Last Played Stations ', data: lastPlayedStations || [] }} />
        <RadioCarousel data={{ label: 'Liked Stations ', data: userLikedStatisonData || [] }} />
        <RadioCarousel data={{ label: 'TopStations By Votes ', data: topStationsByVotes?.results || [] }} />
        <RadioCarousel data={{ label: 'Trending Stations', data: trendingStationsData || [] }} />
        <Typography sx={{ marginTop: '2%', marginBottom: '2%' }}>Browse By Genres</Typography>
        <RadioGenreTabs countryCode={country} genres={genres} />
      </Box>
    </NoSsr>
  );
};

export const getServerSideProps = hasNavigationCSR(async (context: any) => {
  const session = await getSession(context)
  const queryClient = new QueryClient();
  //@ts-ignore
  const userid = session?.user?.id
  let country = 'IN'
  if (!queryClient.getQueryData(['all_countries'])) {
    await Promise.all([
      queryClient.prefetchQuery(['all_countries'], async () => await getAllRadioCountries()),
      queryClient.prefetchQuery(["all_station_genres", country], async () => getAllGenres(country)),
      queryClient.prefetchQuery(["lastPlayedStations", userid], async () => getLastPlayedStations(userid)),
      queryClient.prefetchQuery(["trendingStations", country], async () => trendingStations(country)),
      queryClient.prefetchQuery(["userlikedStations", userid], async () => userLikedStatison(userid)),
      queryClient.prefetchQuery(["browseStationsByCountry", country, 1], async () => browseStationsByCountry(country, 1)),
    ])
  }

  return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
})

export const QueuePage = memo(
  RadioComp
  // propsAreEqual
);
export default QueuePage;

