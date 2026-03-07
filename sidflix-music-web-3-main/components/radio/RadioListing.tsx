
import { Box, Button, Card, CardContent, Typography } from "@mui/material";
import { AppContext, AppContextType } from "../../pages/_app";
import { browseStationsByCountryAndGenre } from "../../api";
import { useInfiniteQuery } from "react-query";
import Grid from '@mui/material/Unstable_Grid2'; // Grid version 2
import { RadioStation } from "../../types/RadioStationType";
import RadioConTextMenu from "../common/RadioConTextMenu";
import Image from 'next/image'
import { useContext, useState } from "react";
interface CompProps {
    countryCode: string,
    genre: string
}

export function RadioListing(props: CompProps) {
    const { setStation } = useContext(
        AppContext
    ) as AppContextType;
    const [hasMore, setHasMore] = useState<boolean>(false)
    const { isSuccess,
        isLoading,
        data,
        fetchNextPage,
        isFetchingNextPage,
        hasNextPage
    } = useInfiniteQuery(
        ["radio_genre_songs", props.countryCode, props.genre],
        async ({ pageParam = 1 }) => await browseStationsByCountryAndGenre(props.countryCode, props.genre, pageParam), {
        getNextPageParam: (lastPage, pages) => {
            return lastPage.page + 1
        },
        keepPreviousData: true,
        refetchOnMount: false,
        refetchOnWindowFocus: false
    });
    return (
        <Box mt={2} ml={1} height={'100vh'} sx={{ overflowY: 'scroll' }} >
            <Grid container spacing={2}>
                {data?.pages?.map(page => page.results?.map((song, index) => <Grid key={'radio-listing-row-' + index + song.id} onClick={() => setStation(song)} sx={{ cursor: 'pointer' }}
                    className=''>
                    <RadioCard station={song} />
                </Grid>))}
            </Grid>

            {hasNextPage && <div style={{ textAlign: 'center', marginTop: '2%' }}>
                <Button color="secondary" variant='outlined' onClick={() => fetchNextPage()} size='small' >Load More</Button>
            </div>}
        </Box>
    )
}



export const RadioCard = ({ station }: { station: RadioStation }) => {
    return <RadioConTextMenu Radio={station ?? undefined} RadioId={station?.id ?? undefined}><Card className='radio_card'
        sx={{
            width: 200, height: 230, display: 'flex',
            flexDirection: 'column',
            alignContent: 'center',
            alignItems: 'center',
            justifyContent: ' space-around',
            '&:hover': {
                cursor: 'pointer'
            },
            background: 'unset'
        }}>
        <Image src={station.favicon || "/sample.jpg"} width={200} height={200} alt="" />
        <CardContent sx={{ color: 'lightgrey', textAlign: 'center', padding: 0, paddingBottom: '3px !important' }}>
            <Typography fontSize={"x-small"}>
                {station.name}
            </Typography>
        </CardContent>
    </Card ></RadioConTextMenu >
}