import * as React from "react";
import NoSsr from "@mui/material/NoSsr";
import { useRouter } from "next/router";
import { Box } from "@mui/material";
import { dehydrate, QueryClient, useQuery } from "react-query";
import { useSession } from "next-auth/react";
import { fetchAlbum } from "../../api";
import { PlayList } from "../../components/playlist/PlayList";


interface PageProps {

}

const AlbumPage: React.FC<PageProps> = ({
}: PageProps) => {
    const { data: session } = useSession()
    React.useEffect(() => {
        if (!session) {
            router.push("/auth/signin");
        }
    }, []);
    const router = useRouter();
    let id: string = '';
    if (router.query.id)
        id = router.query.id as string;
    const { data } = useQuery(['album', id], async () => await fetchAlbum(id));
    return <PlayList fav={false} data={data} />
};


export default AlbumPage;

export async function getServerSideProps(context: any) {
    const playlistid = context.params.id
    const queryClient = new QueryClient();
    await queryClient.prefetchQuery(["album", playlistid], async () =>
        fetchAlbum(playlistid)
    );

    return { props: { dehydratedState: dehydrate(queryClient) } };
}
