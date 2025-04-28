"use client"; 
import SignUpForm from "@/components/auth/SignUpForm";
import { UserProvider } from "@/context/UserContext";
import { Metadata } from "next";



export default function SignUp() {
  return (<UserProvider><SignUpForm /></UserProvider>);
}
