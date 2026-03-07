import * as React from "react";
import NoSsr from "@mui/material/NoSsr";

import { useRouter } from "next/router";
import { Box } from "@mui/material";
import { PlayListCollection } from "../../components/playlist/PlayListCollection";

interface PageProps {

}

const PlaylistComp: React.FC<PageProps> = ({
}: PageProps) => {


    const router = useRouter();
    let playlistId: string;
    if (router.query.id)
        playlistId = router.query.id as string;
    return (
        < >
            <NoSsr>
                <Box sx={{
                    overflowY: 'scroll',
                    
                }}>
                    {/* mid side */}
                    <PlayListCollection playlist={playlistId} />
                </Box>
            </NoSsr>
        </>
    );
};

export const PlaylistPage = React.memo(
    PlaylistComp
    // propsAreEqual
);
export default PlaylistPage;

// export async function getServerSideProps(context: any) {
//     //load user data here

//     let playlistID = "65172d4d-5b42-4dfa-8e5a-035bb4be597a"; //Load this from LS.
//     const queryClient = new QueryClient();
//     if (context.query.id) playlistID = context.query.id;
//     if (context.query.user_playlist) {
//         await queryClient.prefetchQuery(["playlist"], async () =>
//             fetchPlaylistData(playlistID)
//         );
//     } else {
//         await queryClient.prefetchQuery(["playlist"], async () =>
//         getUserPlaylist(playlistID)
//         );
//     }
//     return { props: { dehydratedState: dehydrate(queryClient) } };
// }
