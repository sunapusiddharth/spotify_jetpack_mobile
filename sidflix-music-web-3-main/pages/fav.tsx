import * as React from "react";
import NoSsr from "@mui/material/NoSsr";

import router, { useRouter } from "next/router";
import { Box } from "@mui/material";
import { PlayList } from "../components/playlist/PlayList";
import { dehydrate, QueryClient, useQuery } from "react-query";
import { getSession, useSession } from "next-auth/react";
import { getFav } from "../api";
import { AppContext, AppContextType } from "./_app";


interface PageProps {

}

const Fav: React.FC<PageProps> = ({
}: PageProps) => {
    const { data: session } = useSession()
    React.useEffect(() => {
        if (!session) {
            router.push("/auth/signin");
        }
    }, []);
    const { user, setCurrentSong } = React.useContext(
        AppContext
    ) as AppContextType;
    const { data } = useQuery(['fav'], async () => await getFav(user?.id!));
    return (
        < >
            <NoSsr>
                <Box sx={{
                    overflowY: 'scroll',

                }}>
                    <PlayList fav={true} data={data} />
                </Box>
            </NoSsr>
        </>
    );
};


export default Fav;

export async function getServerSideProps(context: any) {
    const session = await getSession(context)
    //@ts-ignore
    const userid = session?.user?.id
    const queryClient = new QueryClient();
    await queryClient.prefetchQuery(["fav"], async () =>
        getFav(userid)
    );
    return { props: { dehydratedState: dehydrate(queryClient) } };
}
