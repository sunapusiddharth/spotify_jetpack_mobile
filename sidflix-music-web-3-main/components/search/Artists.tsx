import * as React from "react";
import { useInfiniteQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import { Box, Button, Grid, Typography } from "@mui/material";
import { fetchAllArtistData } from "../../api";
import { ArtistCard } from "../../components/artist/ArtistCard";
import Link from "next/link";


export const Artists = ({
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
        ["all_artist"],
        async ({ pageParam = 1 }) => await fetchAllArtistData(pageParam), {
        getNextPageParam: (lastPage, pages) => {
            // console.log("latp", lastPage)
            // setPage(lastPage.page + 1)
            return lastPage.page + 1
        },
        keepPreviousData: true,
        refetchOnMount: false,
        refetchOnWindowFocus: false
    });
    // console.log("Page viewing",data)
    return < >

        <NoSsr>
            <Box sx={{
                overflowY: 'scroll',

            }} mt={2}>
                {isError && <Typography> {'Error!' + error}</Typography>}
                {isLoading && <div>Loading...</div>}
                {isSuccess && data?.pages && <>
                    <Grid container spacing={{ xs: 2, md: 4 }} mt={2} columns={{ xs: 2, md: 5 }} justifyContent='center'>
                        {data.pages?.map((page, i) => (
                            <>
                                {page.results?.map((artist, j) => <Link href={`/artist/${artist.id}`} key={`artistlist-${artist.id}`}><Grid item sx={{ cursor: 'pointer' }}>
                                    <ArtistCard key={`card-listing-${artist.id}`} data={artist} />
                                </Grid>
                                </Link>
                                )}
                            </>
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