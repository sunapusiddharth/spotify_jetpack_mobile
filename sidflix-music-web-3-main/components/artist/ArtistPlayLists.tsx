import { More } from "@mui/icons-material";
import { Box, Grid, IconButton, Typography } from "@mui/material";
import * as React from "react";
import { useQueryClient } from "react-query";
import { fetchAllArtistPlaylistsCollection } from "../../api";
import { PlayListType } from "../../types/PlayListType";
import { ContentCard } from "../home/Card";

interface COompProps {
    artistId: string
}


const ArtistPlaylist: React.FC<COompProps> = (
    props: COompProps
) => {
    const { artistId } = props
    const limit = 50
    const [page, setPage] = React.useState<number>(1)
    const [hasMore, setHasMore] = React.useState<boolean>(true)
    const [cards, setCards] = React.useState<PlayListType[]>([])
    const client = useQueryClient()
    React.useEffect(() => {
        nextPage()
    }, [])
    async function nextPage() {
        const data = await fetchAllArtistPlaylistsCollection(artistId, limit, (page - 1) * limit)
        await client.setQueryData(['artists_playlist', artistId], data)
        if (data?.length) {
            setCards([...cards, ...data])
        } else {
            setHasMore(false)
        }
        setPage(page + 1)
    }

    return (
        <Box>
            <Typography>Featured In Playlists</Typography>
            <Grid>
                {cards?.map(x => <ContentCard data={{
                    image: x.image,
                    id: x.id.toString(),
                    title: x.title,
                    subtitle: '',
                    type: 'playlist_card',
                    path: '/playlist/' + x.id,
                    song: null
                }} />)}
            </Grid>
            {hasMore && <IconButton onClick={nextPage}><More>Load More</More></IconButton>}
        </Box>
    );
};

export default ArtistPlaylist;
