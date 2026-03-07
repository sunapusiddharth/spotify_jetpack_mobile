import * as React from "react";
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import { Card } from '@mui/material'
import { ArtistListingType } from "../../types/ArtistType";
import Image from 'next/image'
import Link from "next/link";
import { useRouter } from "next/router";
interface CompProps {
    data: ArtistListingType
}
function ArtistCardComp(props: CompProps) {
    const router = useRouter()
    const { data } = props
    return <div className='img_container' onClick={() => {
        router.push('/artist/' + data.id)
    }}> <Card
        className='home_carousel_card_artist'
        sx={{
            // width: 200, 
            width: { xs: 150, md: 200 },
            height: 230,
            display: 'flex',
            flexDirection: 'column',
            alignContent: 'center',
            alignItems: 'center',
            justifyContent: ' space-around',
            background: 'unset',
            '&:hover': {
                // backgroundColor: '#282626',
                cursor: 'pointer'
            },
            // cursor:'pointer'
        }} >

            <Image alt="" src={data.image && data.image !== 'no-cover.jpg' ? data.image : "/sample.jpg"}
                 height={230} width={200}
                // fill={true}
                className='imasge' />
            <img src="/play_icon.png" className='play' />
            <CardContent sx={{ fontSize: 'small', padding: 1 }}>
                <Typography variant="body2" textAlign={'center'}>{data.title.length > 30 ? data.title.slice(0, 30) + '...' : data.title}</Typography>
            </CardContent>
        </Card>
    </div>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    // //console.log("Sidhu",prevProps,nextProps);
    return prevProps === nextProps;
}

export const ArtistCard = React.memo(
    ArtistCardComp,
    moviePropsAreEqual
);
