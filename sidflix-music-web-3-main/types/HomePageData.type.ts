import { CardContentType } from "./CardType"

export type HomePageDataType = {
                    cardType: string,
                    label: string,
                    path: string,
                    cards: CardContentType[],
                    id: string
}