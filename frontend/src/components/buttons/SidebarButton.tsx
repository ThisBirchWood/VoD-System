import MenuButton from "./MenuButton.tsx";
import { Link } from "react-router-dom";

type props = {
    url: string;
    logo: React.ReactNode;
    label: String;
}

const SidebarButton = ({url, logo, label}: props) => {
    return (
        <Link
            className={"w-full"}
            to={url}
        >
            <MenuButton className={"flex items-center gap-2 w-full"}>
                {logo}{label}
            </MenuButton>
        </Link>
    )
}

export default SidebarButton;