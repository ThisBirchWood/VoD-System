import { Menu, X } from 'lucide-react';
import MenuButton from "./buttons/MenuButton.tsx";
import clsx from "clsx";
import type {User} from "../utils/types.ts";
import { login } from "../utils/endpoints.ts";
import { Dropdown, DropdownItem } from "./Dropdown.tsx";
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import {useNavigate} from "react-router-dom";

type props = {
    sidebarToggled: boolean;
    setSidebarToggled: Function;
    user: User | null;
    className?: string;
}

const Topbar = ({sidebarToggled, setSidebarToggled, user, className}: props) => {
    const navigate = useNavigate();

    const handleLogout = () => {
        // delete token cookie
        document.cookie = "token=; Secure; SameSite=None; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    }

    return (
        <div className={clsx(className, "flex justify-between")}>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <Menu size={24}/> :  <X size={24}/>}
            </MenuButton>

            { user ? (
                <div>
                    <img
                        className={"w-8 h-8 rounded-full inline-block"}
                        src={user.profilePictureUrl}
                        referrerPolicy="no-referrer"
                    />

                    <Dropdown label={user.name}>
                        <DropdownItem item="Logout"
                                      onClick={() => handleLogout()}
                                      className={"text-red-500 font-medium"} />
                    </Dropdown>
                </div>
            ) :
            (
                <GoogleOAuthProvider
                    clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
                    <GoogleLogin
                        onSuccess={(credentialResponse) => {
                            if (!credentialResponse.credential) {
                                console.error("No credential received from Google Login");
                                return;
                            }
                            login(credentialResponse.credential).then(() => {navigate(0)});
                        }}
                    />
                </GoogleOAuthProvider>
            )}

        </div>
    )
}

export default Topbar;