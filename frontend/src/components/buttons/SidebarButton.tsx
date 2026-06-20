import MenuButton from "./MenuButton.tsx";
import { Link, useLocation } from "react-router-dom";

type props = {
    url: string;
    logo: React.ReactNode;
    label: String;
}

const SidebarButton = ({url, logo, label}: props) => {
    const { pathname } = useLocation();
    const isActive = url === "/" ? pathname === "/" : pathname.startsWith(url);

    return (
        <Link className="w-full" to={url}>
            <MenuButton className={`flex items-center gap-2.5 w-full px-3 py-2 text-sm font-medium rounded-md ${isActive ? "bg-gray-100 text-gray-900" : "text-gray-600"}`}>
                {logo}{label}
            </MenuButton>
        </Link>
    )
}

export default SidebarButton;
