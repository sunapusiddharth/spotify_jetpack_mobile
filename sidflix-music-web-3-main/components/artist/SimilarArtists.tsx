import { Box, Grid } from "@mui/material";
import * as React from "react";
import { ArtistListingType } from "../../types/ArtistType";
import { ContentCard } from "../home/Card";
interface COompProps {
    similar: ArtistListingType[]
}


const SimilarArtists: React.FC<COompProps> = (
    props: COompProps
) => {
    const { similar } = props
    return (
        <Box>
            <Grid>
                {similar?.map(x => <ContentCard data={{
                    image: x.image,
                    id: x.id,
                    title: x.title,
                    subtitle: '',
                    type: 'artists_card',
                    path: '/artist/' + x.id,
                    song: null
                }} />)}
            </Grid>
        </Box>
    );
};

export default SimilarArtists;
