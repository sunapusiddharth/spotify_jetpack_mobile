import * as React from "react";
import { dehydrate, QueryClient, useInfiniteQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import { getAllAvailableSongs } from "../api";
import { useSession } from "next-auth/react";
import { Grid } from "@mui/material";
import { ContentCard } from "../components/home/Card";
import { hasNavigationCSR } from ".";
import router from "next/router";

interface PageProps {
}

const QueueComp: React.FC<PageProps> = (props: PageProps) => {
  const { data: session } = useSession()
  React.useEffect(() => {
    if (!session) {
      router.push("/auth/signin");
    }
  }, []);
  const {
    isLoading, data,
    fetchNextPage,
    isFetchingNextPage
  } = useInfiniteQuery(['getAllAvailableSongs'], async ({ pageParam = 0 }) => await getAllAvailableSongs(pageParam, 100), {
    getNextPageParam: (lastPage, pages) => {
      return lastPage.page + 1
    },
    keepPreviousData: true,
    refetchOnMount: false,
    refetchOnWindowFocus: false,
    staleTime: 9000,
    cacheTime: 90000,
  })


  return (
    <NoSsr>
      <Box mt={2}>
        <Grid spacing={1.5} container>
          {data?.pages?.map(page => page.results?.map((song, index) => <Grid item><ContentCard key={"available_tracks" + song.id + index} data={{
            image: song.thumbnail,
            id: song.id,
            title: song.name,
            subtitle: '',
            type: "music_card",
            path: "",
            song: song
          }} /></Grid>))}
        </Grid>
        <div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
          <Button variant='contained' onClick={() => fetchNextPage()} size='small' sx={{ color: 'white', background: 'purple', fontWeight: 'bolder' }}>Load More</Button>
        </div>
        <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
      </Box>

    </NoSsr >
  );
};

export const getServerSideProps = hasNavigationCSR(async (context: any) => {
  const queryClient = new QueryClient();
  if (!queryClient.getQueryData(['getAllAvailableSongs'])) {
    await queryClient.prefetchInfiniteQuery(["getAllAvailableSongs"], async () => getAllAvailableSongs(0, 100))
  }
  return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
})
export default QueueComp;