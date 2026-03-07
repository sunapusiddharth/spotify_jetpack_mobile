import { Box, Typography } from '@mui/material';
import { styled } from '@mui/material/styles';
import { PodcastEpisodeDto } from '../../types/PodcastEpisode.dto';
import { RadioStation } from '../../types/RadioStationType';
import { SongType } from '../../types/Song.type';
import { MediaType } from '../SongComponent';

const CoverImage = styled('div')({
    width: 100,
    height: 100,
    objectFit: 'cover',
    overflow: 'hidden',
    flexShrink: 0,
    borderRadius: 8,
    backgroundColor: 'rgba(0,0,0,0.08)',
    '& > img': {
        width: '100%',
    },
});
export const CoverImg = ({ song, currentMediaType }: { song: RadioStation | SongType | PodcastEpisodeDto | undefined, currentMediaType: MediaType | undefined }) => {
    let thumbnail = ''
    let title = ''
    let artists = ''
    let album = ''
    switch (currentMediaType) {
        case 'song':
            song = song as SongType
            thumbnail = song?.thumbnail !== 'no-cover.jpg' ? song?.thumbnail : '/sample.jpg'
            title = song?.name
            artists = song?.artists?.map(x => x.title).join(' , ')
            album = song?.album?.title
            break;
        case 'radio':
            song = song as RadioStation
            thumbnail = song?.favicon !== 'no-cover.jpg' ? song?.favicon.startsWith('/') ? 'https://pcradio.ru/' + song?.favicon : song?.favicon : '/sample.jpg'
            title = song?.name
            break;
        case 'podcast':
            song = song as PodcastEpisodeDto
            thumbnail = song?.image_url ?? '/sample.jpg'
            title = song?.name
            break;

        default:
            break;
    }
    return <Box sx={{ display: 'flex', alignItems: 'center' }}>
        {thumbnail && <img
            alt=""
            src={thumbnail}
            height={70}
            width={100}
        />}
        <Box sx={{
            ml: 1.5,
            minWidth: 0,
            display: 'flex',
            maxWidth: '206px',
            flexWrap: 'wrap'
        }}>
            <Typography variant="caption" fontSize={'x-small'} color="white">
                {artists}
            </Typography>
            <Typography  fontSize={'x-small'} color="white" sx={{textWrap:'balance'}}>
                {title}
            </Typography>
            <Typography noWrap letterSpacing={-0.25} fontSize={'x-small'} variant="caption" color="white">
                {album}
            </Typography>
        </Box>
    </Box>
}