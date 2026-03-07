import { useQuery } from "react-query";
import { songGenres } from "../api";
import React, { useState } from "react";
import { AppContext, AppContextType } from "../pages/_app";
import Grid2 from "@mui/material/Unstable_Grid2";
import { Box, Button, Typography } from "@mui/material";

export const SongGenres = () => {
    const { user } = React.useContext(
        AppContext
    ) as AppContextType;
    const [showMore, setShowMore] = useState<boolean>(false)
    const { data: song_genres } = useQuery(["song_genres"], async () => await songGenres(user?.id ?? ''));
    const genres = !showMore ? song_genres?.slice(0, 10) : song_genres?.slice(0, song_genres.length-10)
    if (!genres?.length) return <></>
    return <Box>
        <Typography>Genres</Typography>
        <Grid2 container gap={2}>
            {genres?.map(genre => <Grid2 sx={{
                background: '#4b0070',
                borderRadius: 10,
                '&:hover':{
                    cursor:'pointer',
                    boxShadow: 'lightgray 0px 1px 2px 0px',
                    // background:'lightgray'
                }
            }} p={1}>{genre.value}</Grid2>)}
        </Grid2>
        <Box sx={{ display: 'flex', justifyContent: 'center' }} pt={3}>
            <Button onClick={() => setShowMore(!showMore)} sx={{
                background: '#4b0070 ',
                color:'white',
                borderRadius:10,
                boxShadow: 'lightgray 0px 0.3px 1px 0px',
                '&:hover':{
                    background:'lightgray'
                }
            }} variant="contained" >Show More</Button>
        </Box>
    </Box>
}