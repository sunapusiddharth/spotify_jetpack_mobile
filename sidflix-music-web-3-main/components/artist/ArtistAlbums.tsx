import { More } from "@mui/icons-material";
import { Box, Grid, IconButton, Typography } from "@mui/material";
import * as React from "react";
import { useQueryClient } from "react-query";
import { fetchAllArtistPlaylists } from "../../api";
import { PlayListType } from "../../types/PlayListType";
import { ContentCard } from "../home/Card";
interface COompProps {
    artistId: string
}


const ArtistAlbums: React.FC<COompProps> = (
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
        const data = await fetchAllArtistPlaylists(artistId, limit, (page - 1) * limit)
        await client.setQueryData(['artists_albums', artistId], data)
        if (data?.length) {
            setCards([...cards, ...data])
        } else {
            setHasMore(false)
        }
        setPage(page + 1)
    }

    return (
        <Box>
            <Typography>Albums</Typography>
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

export default ArtistAlbums;
