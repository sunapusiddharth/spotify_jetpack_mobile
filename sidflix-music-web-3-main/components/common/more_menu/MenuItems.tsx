import { Typography } from "@mui/material";
import Link from "next/link";
import * as React from "react";
import { useState, useEffect, useRef } from "react";

import Dropdown from "./Dropdown";
import { MenuItemType } from "./MoreHorizontalMenu";

const MenuItems = ({ items, depthLevel }: { items: MenuItemType, depthLevel: number }) => {
    const [dropdown, setDropdown] = useState(false);

    let ref = useRef();

    useEffect(() => {
        const handler = (event: MouseEvent | TouchEvent) => {
            if (dropdown && ref.current && !ref.current.contains(event.target)) {
                setDropdown(false);
            }
        };
        document.addEventListener("mousedown", handler);
        document.addEventListener("touchstart", handler);
        return () => {
            // Cleanup the event listener
            document.removeEventListener("mousedown", handler);
            document.removeEventListener("touchstart", handler);
        };
    }, [dropdown]);

    const onMouseEnter = () => {
        window.innerWidth > 960 && setDropdown(true);
    };

    const onMouseLeave = () => {
        window.innerWidth > 960 && setDropdown(false);
    };

    return (
        <li
            className="menu-items"
            ref={ref}
            onMouseEnter={onMouseEnter}
            onMouseLeave={onMouseLeave}

        >
            {items.submenu ? (
                <>
                    <button
                        type="button"
                        aria-haspopup="menu"
                        aria-expanded={dropdown ? "true" : "false"}
                        onClick={() => setDropdown((prev) => !prev)}
                    >
                        {items.title}{" "}
                        {depthLevel > 0 ? <span>&raquo;</span> : <span className="arrow" />}
                    </button>
                    <Dropdown
                        depthLevel={depthLevel}
                        submenus={items.submenu}
                        dropdown={dropdown}
                    />
                </>
            ) : (
                <>{items.link ? <Link href={items.link}>{items.title}</Link> :
                    items.onClick ? <Typography>{items.title}</Typography> :
                        <Typography>{items.title}</Typography>
                }</>
            )}
        </li>
    );
};

export default MenuItems;