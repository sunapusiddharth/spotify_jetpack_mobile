import * as React from "react";
import { useInfiniteQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import { Box, Button, Grid, Typography } from "@mui/material";
import { fetchAllPlayListData } from "../../api";
import { ArtistCard } from "../../components/artist/ArtistCard";
import Link from "next/link";



export const AlbumsSearch = ({
}: {}) => {


    const { isSuccess,
        isLoading,
        isError,
        data,
        error,
        hasNextPage,
        fetchNextPage,
        isFetchingNextPage
    } = useInfiniteQuery(
        ["top_albums"],
        async ({ pageParam = 1 }) => await fetchAllPlayListData(pageParam), {
        getNextPageParam: (lastPage, pages) => {
            // console.log("latp", lastPage)
            // setPage(lastPage.page + 1)
            return lastPage.page + 1
        },
        keepPreviousData: true,
        refetchOnMount: false,
        refetchOnWindowFocus: false

    });
    return < >
        <NoSsr>
            <Box sx={{
                overflowY: 'scroll',

            }} mt={2}>
                {isError && <Typography> {'Error!' + error}</Typography>}
                {isLoading && <div>Loading...</div>}
                {isSuccess && data?.pages && <>
                    <Grid container spacing={{ xs: 2, md: 2 }} columns={{ xs: 2, md: 5 }} p={2}>
                        {data.pages?.map((page, i) => page.results?.map((artist, j) => <Link href={`/playlist/${artist.id}`} key={'artist-' + artist.id}>
                                    <Grid item key={'playlists-' + artist.id} sx={{ cursor: 'pointer' }}>
                                        <ArtistCard key={`card-listing-${artist.id}`} data={artist} />
                                    </Grid>
                                </Link>
                                ))}
                    </Grid>

                    {data.pages[data.pages.length - 1].results.length && <><div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
                        <Button variant='text' onClick={() => fetchNextPage()} size='small' sx={{ color: 'purple', fontWeight: 'bolder' }}>Load More</Button>
                    </div>
                        <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
                    </>}
                </>}
            </Box>

        </NoSsr>
    </>
};

// export async function getServerSideProps(context: any) {
//     const queryClient = new QueryClient();
//     await queryClient.prefetchQuery(["all_playlist"], async () =>
//         fetchAllPlayListData('1')
//     );

//     return { props: { dehydratedState: dehydrate(queryClient) } };
// }
