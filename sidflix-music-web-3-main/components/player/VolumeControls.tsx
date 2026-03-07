import { VolumeDownRounded, VolumeUpRounded } from "@mui/icons-material"
import { Stack, Slider, useTheme } from "@mui/material"
import ReactPlayer from "react-player"

export const VolumneControls = (
    { playerRef
    }: {

        playerRef: React.RefObject<ReactPlayer>
    }
) => {
    const theme = useTheme();
    const lightIconColor =
        theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.4)' : 'rgba(0,0,0,0.4)';
    return <Stack spacing={2} direction="row" sx={{ mb: 1, px: 1 }} alignItems="center">
        <VolumeDownRounded htmlColor={lightIconColor} />
        <Slider
            aria-label="Volume"
            defaultValue={30}
            onChange={(_, value) => {
                playerRef.current?.setState({ volume: 1 })
            }}
            sx={{
                color: theme.palette.mode === 'dark' ? '#fff' : 'rgba(0,0,0,0.87)',
                '& .MuiSlider-track': {
                    border: 'none',
                },
                '& .MuiSlider-thumb': {
                    width: 24,
                    height: 24,
                    backgroundColor: '#fff',
                    '&:before': {
                        boxShadow: '0 4px 8px rgba(0,0,0,0.4)',
                    },
                    '&:hover, &.Mui-focusVisible, &.Mui-active': {
                        boxShadow: 'none',
                    },
                },
            }}
        />
        <VolumeUpRounded htmlColor={lightIconColor} />
    </Stack>
}