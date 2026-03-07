import * as React from "react";
import { useQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";

import { useRouter } from "next/router";
import { Box } from "@mui/material";
import { fetchArtistData } from "../../api";
import { ArtistCover } from "../../components/artist/Artist";
import { AppContext, AppContextType } from "../_app";
import ArtistTabs from "../../components/artist/ArtistTabs";
interface PageProps {
}
const ArtistPageComp: React.FC<PageProps> = ({
}: PageProps) => {

    const router = useRouter();
    const { user } = React.useContext(
        AppContext
    ) as AppContextType;
    let artistId: string = '';
    if (router.query.id)
        artistId = router.query.id as string;
    const {
        data,
        isLoading,
        isError,
        error,
    } = useQuery(
        ["artist", artistId],
        async () => await fetchArtistData(artistId),
        {
            keepPreviousData: true,
            refetchOnMount: false,
            refetchOnWindowFocus: false,
        }
    );
    return < >

        <NoSsr>
            <Box sx={{
                overflowY: 'scroll',
                
            }}>
                {(data && user) && <><ArtistCover artist={data} songs={data.popular_songs} />
                    <ArtistTabs artist={data} />
                </>
                }
            </Box>
        </NoSsr>
    </>
};

export const ArtistPage = React.memo(
    ArtistPageComp
    // propsAreEqual
);
export default ArtistPage;

// export async function getServerSideProps(context: any) {
//     //load user data here
//     let artistId = "65172d4d-5b42-4dfa-8e5a-035bb4be597a"; //Load this from LS.
//     if (context.query.id) artistId = context.query.id;
//     const queryClient = new QueryClient();
//     await queryClient.prefetchQuery(["artist", artistId], async () =>
//         fetchArtistData(artistId)
//     );

//     return { props: { dehydratedState: dehydrate(queryClient) } };
// }
