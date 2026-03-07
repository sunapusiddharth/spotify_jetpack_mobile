import { FastRewindRounded, PlayArrowRounded, PauseRounded, FastForwardRounded } from "@mui/icons-material"
import { Box, IconButton, useTheme } from "@mui/material"

export const Buttons = (
    {
        playing,
        handlePrev, handleNext, setPlaying
    }: {
        playing: boolean,
        handlePrev: any, handleNext: any
        setPlaying: (s: boolean) => void,
    }
) => {
    const theme = useTheme();
    const mainIconColor = theme.palette.mode === 'dark' ? '#fff' : '#000';

    return <Box
        sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mt: -1,
        }}
    >
        <IconButton aria-label="previous song" onClick={handlePrev}>
            <FastRewindRounded fontSize="large" htmlColor={mainIconColor} />
        </IconButton>
        <IconButton
            onClick={() => {
                !playing ? setPlaying(true) : setPlaying(false)
            }}
            aria-label={!playing ? 'play' : 'pause'}
        >
            {!playing ? (
                <PlayArrowRounded
                    sx={{ fontSize: '3rem' }}
                    htmlColor={mainIconColor}
                />
            ) : (
                <PauseRounded sx={{ fontSize: '3rem' }} htmlColor={mainIconColor} />
            )}
        </IconButton>
        <IconButton aria-label="next song" onClick={handleNext}>
            <FastForwardRounded fontSize="large" htmlColor={mainIconColor} />
        </IconButton>
    </Box>
}