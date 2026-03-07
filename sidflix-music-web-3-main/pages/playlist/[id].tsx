import * as React from "react";
import NoSsr from "@mui/material/NoSsr";
import { useRouter } from "next/router";
import { Box } from "@mui/material";
import { dehydrate, QueryClient, useQuery } from "react-query";
import { useSession } from "next-auth/react";
import { fetchPlaylistData } from "../../api";
import { PlayList } from "../../components/playlist/PlayList";


interface PageProps {

}

const PlayListPage: React.FC<PageProps> = ({
}: PageProps) => {
    const { data: session } = useSession()
    React.useEffect(() => {
        if (!session) {
            router.push("/auth/signin");
        }
    }, []);
    const router = useRouter();
    let playlistId: string = '';
    if (router.query.id)
        playlistId = router.query.id as string;
    const { data } = useQuery(['playlist', playlistId], async () => await fetchPlaylistData(playlistId));
    return (
        < >
            <NoSsr>
                <Box sx={{
                    overflowY: 'scroll',

                }}>
                    <PlayList fav={false} data={data} />
                </Box>
            </NoSsr>
        </>
    );
};


export default PlayListPage;

export async function getServerSideProps(context: any) {
    const playlistid = context.params.id
    const queryClient = new QueryClient();
    await queryClient.prefetchQuery(["playlist", playlistid], async () =>
        fetchPlaylistData(playlistid)
    );

    return { props: { dehydratedState: dehydrate(queryClient) } };
}
