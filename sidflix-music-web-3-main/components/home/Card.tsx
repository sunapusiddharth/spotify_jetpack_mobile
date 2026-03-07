import * as React from "react";
import { CardContentType } from "../../types/CardType";
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import { Card } from '@mui/material'
import { AppContext, AppContextType } from "../../pages/_app";
import Image from "next/image";
import ContextMenu from "../common/ConTextMenu";
import { checkIfUserHasLikedSong } from "../../utils";
import router from "next/router";

interface CompProps {
    data: CardContentType
}
function CardComp(props: CompProps) {
    const { setCurrentSong, user } = React.useContext(
        AppContext
    ) as AppContextType;
    const { data } = props
    const navigateToPlaylist = (path: string, type: string) => {
        let basepath = '/playlist/';
        if (type == 'playlist_collection_card') basepath = '/playlist_collection/'
        router.push(
            {
                pathname: basepath + path,
            },
            undefined,
            { shallow: true }
        );
    }

    return <div className='img_container'><ContextMenu song={data.song ?? undefined} songId={data.song?.id ?? undefined}>
        <Card className='home_carousel_casrd'
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
            }}
            onClick={() => data.type == 'music_card' ? setCurrentSong(data.song) : navigateToPlaylist(data.id, data.type)}
        >

            <Image alt="" src={data.image && data.image !== 'no-cover.jpg' ? data.image : "/sample.jpg"}
                className='imsage' width={200} height={200}
                style={{ filter: `${data.song?.s3link ? 'grayscale(0%)' : 'grayscale(90%)'}` }}
            />
            <img src="/play_icon.png" className='play_home' />
            <img src="/love.png" className={!checkIfUserHasLikedSong(user, data?.song?.id) ? 'user-liked-song' : 'user-neutral-song'} />

            <CardContent sx={{ fontSize: 'small', textAlign: 'center', padding: 0, paddingBottom: '3px !important' }}>
                <Typography variant="caption" >
                    {data.title}
                </Typography>
                <span>{data.subtitle} </span><br />
                <span style={{ color: 'darkgray', fontSize: 'smaller' }}>{data.song?.artists?.map(x => x.title)} {data.song?.album?.title && data.song?.album?.title !== 'singles' ? ' , ' + data.song?.album?.title : ''}</span><br />
            </CardContent>
        </Card>
    </ContextMenu>
    </div>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    return prevProps === nextProps;
}

export const ContentCard = React.memo(
    CardComp,
    moviePropsAreEqual
);
